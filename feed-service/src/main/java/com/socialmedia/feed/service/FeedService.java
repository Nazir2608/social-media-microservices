package com.socialmedia.feed.service;

import com.socialmedia.feed.client.GraphClient;
import com.socialmedia.feed.client.PostClient;
import com.socialmedia.feed.client.UserClient;
import com.socialmedia.feed.dto.FeedItemResponse;
import com.socialmedia.feed.dto.PagedResponse;
import com.socialmedia.feed.kafka.PostCreatedEvent;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedService {

    private final StringRedisTemplate redisTemplate;
    private final GraphClient graphClient;
    private final PostClient postClient;
    private final UserClient userClient;

    @Value("${kafka.topics.post-created:post_created}")
    private String postCreatedTopic;

    public void handlePostCreated(PostCreatedEvent event) {
        if (event == null || event.getPostId() == null || event.getUserId() == null) {
            return;
        }
        Long authorId = event.getUserId();
        List<Long> followers = graphClient.fetchFollowerIds(authorId);
        Set<Long> targetUserIds = new HashSet<>(followers);
        targetUserIds.add(authorId);
        long score = event.getCreatedAt() != null ? event.getCreatedAt().toEpochMilli() : Instant.now().toEpochMilli();
        String member = String.valueOf(event.getPostId());
        ZSetOperations<String, String> zset = redisTemplate.opsForZSet();
        for (Long userId : targetUserIds) {
            String key = feedKey(userId);
            zset.add(key, member, score);
        }
        log.debug("Added postId={} to feeds of {} users", event.getPostId(), targetUserIds.size());
    }

    public PagedResponse<FeedItemResponse> getFeed(Long userId, int page, int size) {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }
        int safeSize = Math.min(Math.max(size, 1), 100);
        String key = feedKey(userId);
        Long total = redisTemplate.opsForZSet().zCard(key);
        long totalElements = total != null ? total : 0L;
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / safeSize);
        int requestedPage = Math.max(page, 0);
        int maxPage = totalPages == 0 ? 0 : totalPages - 1;
        int safePage = Math.min(requestedPage, Math.max(maxPage, 0));
        long start = (long) safePage * safeSize;
        long end = start + safeSize - 1L;
        Set<String> range = redisTemplate.opsForZSet().reverseRange(key, start, end);
        if (range == null || range.isEmpty()) {
            return PagedResponse.<FeedItemResponse>builder()
                    .content(new ArrayList<>())
                    .page(safePage)
                    .size(safeSize)
                    .totalElements(totalElements)
                    .totalPages(totalPages)
                    .build();
        }
        List<Long> postIds = range.stream()
                .map(Long::valueOf)
                .toList();
        List<PostClient.PostResponse> posts = new ArrayList<>();
        for (Long postId : postIds) {
            PostClient.PostResponse post = postClient.fetchPost(postId);
            if (post != null) {
                posts.add(post);
            }
        }
        Set<Long> userIds = new HashSet<>();
        for (PostClient.PostResponse post : posts) {
            if (post.getUserId() != null) {
                userIds.add(post.getUserId());
            }
        }
        Map<Long, String> usernameById = new HashMap<>();
        for (Long uid : userIds) {
            UserClient.UserProfileResponse profile = userClient.fetchUser(uid);
            if (profile != null && profile.getUsername() != null) {
                usernameById.put(uid, profile.getUsername());
            }
        }
        List<FeedItemResponse> items = new ArrayList<>();
        for (PostClient.PostResponse post : posts) {
            String username = usernameById.get(post.getUserId());
            FeedItemResponse item = FeedItemResponse.builder()
                    .postId(post.getId())
                    .caption(post.getCaption())
                    .mediaUrl(post.getMediaUrl())
                    .username(username)
                    .build();
            items.add(item);
        }
        return PagedResponse.<FeedItemResponse>builder()
                .content(items)
                .page(safePage)
                .size(safeSize)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .build();
    }

    private String feedKey(Long userId) {
        return "feed:" + userId;
    }
}

