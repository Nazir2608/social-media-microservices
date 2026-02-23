package com.socialmedia.feed.client;

import java.util.Collections;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostClient {

    private final RestTemplate restTemplate;

    @Value("${services.post.base-url}")
    private String postBaseUrl;

    public PostResponse fetchPost(Long postId) {
        String url = postBaseUrl + "/api/posts/" + postId;
        try {
            ResponseEntity<ApiResponse<PostResponse>> response = restTemplate.exchange(url, HttpMethod.GET,
                    null, new ParameterizedTypeReference<ApiResponse<PostResponse>>() {
                    }
            );
            ApiResponse<PostResponse> body = response.getBody();
            if (body == null || body.getData() == null) {
                return null;
            }
            return body.getData();
        } catch (Exception ex) {
            log.error("Failed to fetch post for postId={}", postId, ex);
            return null;
        }
    }

    @Data
    private static class ApiResponse<T> {
        private boolean success;
        private T data;
    }

    @Data
    public static class PostResponse {
        private Long id;
        private Long userId;
        private String caption;
        private String mediaUrl;
    }
}

