package com.socialmedia.post.dto;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PostResponse {

    private Long id;

    private Long userId;

    private String caption;

    private String mediaUrl;

    private Instant createdAt;

    private List<String> hashtags;
}

