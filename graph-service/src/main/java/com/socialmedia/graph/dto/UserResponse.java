package com.socialmedia.graph.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UserResponse {

    private Long userId;

    private String username;

    private LocalDateTime createdAt;
}

