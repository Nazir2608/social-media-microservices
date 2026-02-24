package com.socialmedia.post.service;

import com.socialmedia.post.dto.CreatePostResponse;
import com.socialmedia.post.dto.PagedResponse;
import com.socialmedia.post.dto.PostResponse;
import com.socialmedia.post.entity.Hashtag;
import com.socialmedia.post.entity.Post;
import com.socialmedia.post.entity.PostHashtag;
import com.socialmedia.post.entity.PostHashtagLink;
import com.socialmedia.post.kafka.PostCreatedEvent;
import com.socialmedia.post.kafka.PostEventPublisher;
import com.socialmedia.post.repository.HashtagRepository;
import com.socialmedia.post.repository.PostHashtagLinkRepository;
import com.socialmedia.post.repository.PostRepository;
import com.socialmedia.post.storage.MediaStorageService;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final HashtagRepository hashtagRepository;
    private final PostHashtagLinkRepository postHashtagLinkRepository;
    private final MediaStorageService mediaStorageService;
    private final PostEventPublisher postEventPublisher;

    @Value("${media.max-size-mb:10}")
    private long maxFileSizeMb;

    @Value("${media.allowed-content-types:image/jpeg,image/png,image/jpg}")
    private String allowedContentTypesProperty;

    @Transactional
    public CreatePostResponse createPost(Long userId, String caption, String hashtagsCsv, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.warn("Create post failed: missing file for userId={}", userId);
            throw new IllegalArgumentException("File is required");
        }
        long maxBytes = maxFileSizeMb * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            log.warn("Create post failed: file too large size={} bytes, limit={} MB for userId={}",
                    file.getSize(), maxFileSizeMb, userId);
            throw new IllegalArgumentException("File size exceeds allowed limit of " + maxFileSizeMb + " MB");
        }
        String contentType = file.getContentType();
        Set<String> allowedContentTypes = new HashSet<>(Arrays.asList(allowedContentTypesProperty.split(",")));
        if (contentType == null || !allowedContentTypes.contains(contentType)) {
            log.warn("Create post failed: unsupported content type={} for userId={}", contentType, userId);
            throw new IllegalArgumentException("Unsupported content type");
        }
        log.debug("Storing media for userId={}, originalFilename={}", userId, file.getOriginalFilename());
        String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        String mediaUrl;
        try {
            mediaUrl = mediaStorageService.store(filename, file.getInputStream());
        } catch (IOException ex) {
            log.error("Failed to store media file {}", filename, ex);
            throw new IllegalStateException("Failed to store media file", ex);
        }
        Post post = new Post();
        post.setUserId(userId);
        post.setCaption(caption);
        post.setMediaUrl(mediaUrl);
        post.setCreatedAt(Instant.now());
        Post savedPost = postRepository.save(post);
        log.info("Post persisted id={} for userId={}", savedPost.getId(), savedPost.getUserId());
        List<String> tags = parseHashtags(hashtagsCsv);
        List<String> normalizedTags = new ArrayList<>();
        for (String tag : tags) {
            String normalized = normalizeTag(tag);
            if (normalized.isEmpty()) {
                continue;
            }
            Hashtag hashtag = hashtagRepository.findByTag(normalized).orElseGet(() -> {
                        Hashtag h = new Hashtag();
                        h.setTag(normalized);
                        return hashtagRepository.save(h);
                    });
            normalizedTags.add(hashtag.getTag());
            PostHashtagLink link = new PostHashtagLink(new PostHashtag(savedPost.getId(), hashtag.getId()));
            postHashtagLinkRepository.save(link);
        }

        PostCreatedEvent event = new PostCreatedEvent(savedPost.getId(), savedPost.getUserId(),savedPost.getCaption(),normalizedTags, savedPost.getCreatedAt());
        postEventPublisher.publishPostCreated(event);
        log.debug("PostCreatedEvent published for postId={}", savedPost.getId());
        return new CreatePostResponse(savedPost.getId(), savedPost.getMediaUrl());
    }

    @Transactional(readOnly = true)
    public PostResponse getPost(Long id) {
        Post post = postRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Post not found"));
        List<PostHashtagLink> links = postHashtagLinkRepository.findAll().stream().filter(link -> link.getId().getPostId().equals(id)).collect(Collectors.toList());
        List<Long> hashtagIds = links.stream().map(link -> link.getId().getHashtagId()).collect(Collectors.toList());
        List<String> tags = hashtagRepository.findAllById(hashtagIds).stream().map(Hashtag::getTag).collect(Collectors.toList());
        log.debug("Loaded post id={} with {} hashtags", id, tags.size());
        return new PostResponse(post.getId(), post.getUserId(), post.getCaption(), post.getMediaUrl(),post.getCreatedAt(), tags);
    }

    @Transactional
    public void deletePost(Long id) {
        Optional<Post> postOpt = postRepository.findById(id);
        if (postOpt.isEmpty()) {
            log.warn("Delete ignored, post id={} not found", id);
            return;
        }
        List<PostHashtagLink> links = postHashtagLinkRepository.findAll().stream().filter(link -> link.getId().getPostId().equals(id)).collect(Collectors.toList());
        postHashtagLinkRepository.deleteAll(links);
        postRepository.deleteById(id);
        log.info("Deleted post id={} and {} hashtag links", id, links.size());
    }

    @Transactional(readOnly = true)
    public PagedResponse<PostResponse> listPosts(int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), 100);

        long total = postRepository.count();
        int totalPages = total == 0 ? 0 : (int) Math.ceil((double) total / safeSize);
        int requestedPage = Math.max(page, 0);
        int maxPage = totalPages == 0 ? 0 : totalPages - 1;
        int safePage = Math.min(requestedPage, Math.max(maxPage, 0));

        Page<Post> pageResult = postRepository.findAll(PageRequest.of(safePage, safeSize));

        List<Long> postIds = pageResult.getContent().stream()
                .map(Post::getId)
                .collect(Collectors.toList());

        List<PostHashtagLink> allLinks = postHashtagLinkRepository.findAll().stream()
                .filter(link -> postIds.contains(link.getId().getPostId()))
                .collect(Collectors.toList());

        List<Long> hashtagIds = allLinks.stream()
                .map(link -> link.getId().getHashtagId())
                .distinct()
                .collect(Collectors.toList());

        List<Hashtag> hashtags = hashtagRepository.findAllById(hashtagIds);
        log.debug("Listing posts page={}, size={}, totalElements={}", safePage, safeSize, total);
        return new PagedResponse<>(
                pageResult.getContent().stream()
                        .map(post -> {
                            List<String> tagsForPost = allLinks.stream()
                                    .filter(link -> link.getId().getPostId().equals(post.getId()))
                                    .map(link -> link.getId().getHashtagId())
                                    .map(id -> hashtags.stream()
                                            .filter(h -> h.getId().equals(id))
                                            .findFirst()
                                            .map(Hashtag::getTag)
                                            .orElse(null))
                                    .filter(tag -> tag != null)
                                    .collect(Collectors.toList());

                            return new PostResponse(
                                    post.getId(),
                                    post.getUserId(),
                                    post.getCaption(),
                                    post.getMediaUrl(),
                                    post.getCreatedAt(),
                                    tagsForPost
                            );
                        })
                        .collect(Collectors.toList()),
                safePage,
                safeSize,
                total,
                totalPages
        );
    }

    private List<String> parseHashtags(String csv) {
        if (csv == null || csv.isBlank()) {
            return List.of();
        }
        return Arrays.stream(csv.split(",")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
    }

    private String normalizeTag(String tag) {
        String trimmed = tag.trim();
        if (trimmed.startsWith("#")) {
            trimmed = trimmed.substring(1);
        }
        return trimmed.toLowerCase(Locale.ROOT);
    }
}
