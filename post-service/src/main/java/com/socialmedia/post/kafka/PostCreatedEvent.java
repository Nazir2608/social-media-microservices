package com.socialmedia.post.kafka;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostCreatedEvent {

    private Long postId;

    private Long userId;

    private String caption;

    private List<String> hashtags;

    private Instant createdAt;
}

