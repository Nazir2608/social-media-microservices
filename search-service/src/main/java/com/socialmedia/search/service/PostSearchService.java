package com.socialmedia.search.service;

import com.socialmedia.search.document.PostDocument;
import com.socialmedia.search.repository.PostSearchRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostSearchService {

    private final PostSearchRepository repository;

    public void indexPost(PostDocument doc) {
        repository.save(doc);
    }

    public Page<PostDocument> searchByText(String query, int page, int size) {
        String q = query == null ? "" : query.toLowerCase(Locale.ROOT);
        int safeSize = Math.min(Math.max(size, 1), 100);

        List<PostDocument> all = new ArrayList<>();
        repository.findAll().forEach(all::add);

        List<PostDocument> matched = all.stream()
                .filter(doc -> {
                    boolean captionMatch = doc.getCaption() != null && doc.getCaption().toLowerCase(Locale.ROOT).contains(q);
                    boolean hashtagMatch = doc.getHashtags() != null && doc.getHashtags().stream()
                            .anyMatch(tag -> tag != null && tag.toLowerCase(Locale.ROOT).contains(q));
                    return captionMatch || hashtagMatch;
                })
                .sorted((a, b) -> {
                    if (a.getCreatedAt() == null && b.getCreatedAt() == null) {
                        return 0;
                    }
                    if (a.getCreatedAt() == null) {
                        return 1;
                    }
                    if (b.getCreatedAt() == null) {
                        return -1;
                    }
                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                })
                .toList();

        int safePage = Math.max(page, 0);
        int fromIndex = Math.min(safePage * safeSize, matched.size());
        int toIndex = Math.min(fromIndex + safeSize, matched.size());
        List<PostDocument> pageContent = matched.subList(fromIndex, toIndex);

        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        return new PageImpl<>(pageContent, pageable, matched.size());
    }

    public Page<PostDocument> searchByHashtag(String hashtag, int page, int size) {
        String q = hashtag == null ? "" : hashtag.toLowerCase(Locale.ROOT);
        int safeSize = Math.min(Math.max(size, 1), 100);

        List<PostDocument> all = new ArrayList<>();
        repository.findAll().forEach(all::add);

        List<PostDocument> matched = all.stream().filter(doc -> doc.getHashtags() != null
                        && doc.getHashtags().stream().anyMatch(tag -> tag != null && tag.toLowerCase(Locale.ROOT).contains(q)))
                .sorted((a, b) -> {
                    if (a.getCreatedAt() == null && b.getCreatedAt() == null) {
                        return 0;
                    }
                    if (a.getCreatedAt() == null) {
                        return 1;
                    }
                    if (b.getCreatedAt() == null) {
                        return -1;
                    }
                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                })
                .toList();

        int safePage = Math.max(page, 0);
        int fromIndex = Math.min(safePage * safeSize, matched.size());
        int toIndex = Math.min(fromIndex + safeSize, matched.size());
        List<PostDocument> pageContent = matched.subList(fromIndex, toIndex);

        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        return new PageImpl<>(pageContent, pageable, matched.size());
    }

    public PostDocument fromEvent(Long postId, String caption, java.util.List<String> hashtags, java.time.Instant createdAt) {
        LocalDate created = createdAt != null ? LocalDateTime.ofInstant(createdAt, ZoneOffset.UTC).toLocalDate() : null;
        return PostDocument.builder()
                .postId(postId)
                .caption(caption)
                .hashtags(hashtags)
                .createdAt(created)
                .build();
    }
}
