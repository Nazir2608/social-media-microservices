package com.socialmedia.graph.controller;

import com.socialmedia.graph.dto.ApiResponse;
import com.socialmedia.graph.dto.PagedResponse;
import com.socialmedia.graph.dto.UserResponse;
import com.socialmedia.graph.service.GraphService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/graph")
@RequiredArgsConstructor
@Validated
@Slf4j
public class GraphController {

    private final GraphService graphService;

    @PostMapping("/follow/{userId}")
    public ResponseEntity<ApiResponse<Void>> follow(
            @RequestHeader("X-User-Id") @NotNull Long currentUserId,
            @PathVariable @Min(1) Long userId) {

        log.info("Follow request from {} to {}", currentUserId, userId);
        graphService.follow(currentUserId, userId);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .data(null)
                .message("Followed")
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/unfollow/{userId}")
    public ResponseEntity<ApiResponse<Void>> unfollow(
            @RequestHeader("X-User-Id") @NotNull Long currentUserId,
            @PathVariable @Min(1) Long userId) {

        log.info("Unfollow request from {} to {}", currentUserId, userId);
        graphService.unfollow(currentUserId, userId);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .data(null)
                .message("Unfollowed")
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}/followers")
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> followers(
            @PathVariable @Min(1) Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("Fetching followers for userId={} page={} size={}", userId, page, size);
        PagedResponse<UserResponse> paged = graphService.getFollowers(userId, page, size);
        ApiResponse<PagedResponse<UserResponse>> response = ApiResponse.<PagedResponse<UserResponse>>builder()
                .success(true)
                .data(paged)
                .message("Followers fetched")
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}/following")
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> following(
            @PathVariable @Min(1) Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("Fetching following for userId={} page={} size={}", userId, page, size);
        PagedResponse<UserResponse> paged = graphService.getFollowing(userId, page, size);
        ApiResponse<PagedResponse<UserResponse>> response = ApiResponse.<PagedResponse<UserResponse>>builder()
                .success(true)
                .data(paged)
                .message("Following fetched")
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }
}

