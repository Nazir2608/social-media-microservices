package com.socialmedia.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreatePostRequest {

    @NotNull
    private Long userId;

    private String caption;

    private String hashtags;
}

