package com.socialmedia.post.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreatePostResponse {

    private Long postId;

    private String mediaUrl;
}

