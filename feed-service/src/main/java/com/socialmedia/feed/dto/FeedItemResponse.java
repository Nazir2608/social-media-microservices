package com.socialmedia.feed.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class FeedItemResponse {
    private Long postId;
    private String caption;
    private String mediaUrl;
    private String username;
}

