package com.socialmedia.feed.controller;

import com.socialmedia.feed.dto.ApiResponse;
import com.socialmedia.feed.dto.FeedItemResponse;
import com.socialmedia.feed.dto.PagedResponse;
import com.socialmedia.feed.service.FeedService;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feed")
@RequiredArgsConstructor
@Validated
@Slf4j
public class FeedController {

    private final FeedService feedService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<FeedItemResponse>>> getFeed(
            @RequestHeader("X-User-Id") Long currentUserId,
            @RequestParam(value = "page", defaultValue = "0") @Min(0) int page,
            @RequestParam(value = "size", defaultValue = "20") @Min(1) int size
    ) {
        log.info("Fetching feed for userId={} page={} size={}", currentUserId, page, size);
        PagedResponse<FeedItemResponse> feed = feedService.getFeed(currentUserId, page, size);
        ApiResponse<PagedResponse<FeedItemResponse>> body = ApiResponse.<PagedResponse<FeedItemResponse>>builder()
                .success(true)
                .data(feed)
                .message("Feed fetched")
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(body);
    }
}

