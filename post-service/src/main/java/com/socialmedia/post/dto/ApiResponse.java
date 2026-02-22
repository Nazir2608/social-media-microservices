package com.socialmedia.post.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;

    private T data;

    private String message;

    private LocalDateTime timestamp;
}

