package com.socialmedia.graph.service;

import com.socialmedia.graph.dto.PagedResponse;
import com.socialmedia.graph.dto.UserResponse;
import com.socialmedia.graph.exception.BadRequestException;
import com.socialmedia.graph.model.UserNode;
import com.socialmedia.graph.repository.GraphRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GraphService {

    private final GraphRepository repository;

    @Transactional(transactionManager = "transactionManager")
    public void follow(Long currentUserId, Long targetUserId) {
        if (currentUserId == null || targetUserId == null) {
            throw new BadRequestException("User ids are required");
        }
        if (currentUserId.equals(targetUserId)) {
            throw new BadRequestException("You cannot follow yourself");
        }
        log.info("User {} follows {}", currentUserId, targetUserId);
        repository.follow(currentUserId, targetUserId);
    }

    @Transactional(transactionManager = "transactionManager")
    public void unfollow(Long currentUserId, Long targetUserId) {
        if (currentUserId == null || targetUserId == null) {
            throw new BadRequestException("User ids are required");
        }
        log.info("User {} unfollows {}", currentUserId, targetUserId);
        repository.unfollow(currentUserId, targetUserId);
    }

    @Transactional(readOnly = true, transactionManager = "transactionManager")
    public PagedResponse<UserResponse> getFollowers(Long userId, int page, int size) {
        if (userId == null) {
            throw new BadRequestException("userId is required");
        }
        int safeSize = Math.min(Math.max(size, 1), 100);

        long total = repository.countFollowers(userId);
        int totalPages = total == 0 ? 0 : (int) Math.ceil((double) total / safeSize);
        int requestedPage = Math.max(page, 0);
        int maxPage = totalPages == 0 ? 0 : totalPages - 1;
        int safePage = Math.min(requestedPage, Math.max(maxPage, 0));
        long offset = (long) safePage * safeSize;

        List<UserNode> nodes = repository.findFollowersPaged(userId, offset, safeSize);

        List<UserResponse> content = nodes.stream()
                .map(this::mapToResponse)
                .toList();

        return PagedResponse.<UserResponse>builder()
                .content(content)
                .page(safePage)
                .size(safeSize)
                .totalElements(total)
                .totalPages(totalPages)
                .build();
    }

    @Transactional(readOnly = true, transactionManager = "transactionManager")
    public PagedResponse<UserResponse> getFollowing(Long userId, int page, int size) {
        if (userId == null) {
            throw new BadRequestException("userId is required");
        }
        int safeSize = Math.min(Math.max(size, 1), 100);

        long total = repository.countFollowing(userId);
        int totalPages = total == 0 ? 0 : (int) Math.ceil((double) total / safeSize);
        int requestedPage = Math.max(page, 0);
        int maxPage = totalPages == 0 ? 0 : totalPages - 1;
        int safePage = Math.min(requestedPage, Math.max(maxPage, 0));
        long offset = (long) safePage * safeSize;

        List<UserNode> nodes = repository.findFollowingPaged(userId, offset, safeSize);

        List<UserResponse> content = nodes.stream()
                .map(this::mapToResponse)
                .toList();

        return PagedResponse.<UserResponse>builder()
                .content(content)
                .page(safePage)
                .size(safeSize)
                .totalElements(total)
                .totalPages(totalPages)
                .build();
    }

    private UserResponse mapToResponse(UserNode node) {
        return UserResponse.builder()
                .userId(node.getUserId())
                .username(node.getUsername())
                .createdAt(node.getCreatedAt() != null ? node.getCreatedAt().toLocalDateTime() : null)
                .build();
    }
}
