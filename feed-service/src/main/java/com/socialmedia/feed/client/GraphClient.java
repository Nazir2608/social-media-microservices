package com.socialmedia.feed.client;

import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
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
public class GraphClient {

    private final RestTemplate restTemplate;

    @Value("${services.graph.base-url}")
    private String graphBaseUrl;

    public List<Long> fetchFollowerIds(Long userId) {
        String url = graphBaseUrl + "/api/graph/" + userId + "/followers?page=0&size=1000";
        try {
            ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> response = restTemplate.exchange(url, HttpMethod.GET,
                    null, new ParameterizedTypeReference<ApiResponse<PagedResponse<UserResponse>>>() {
                    }
            );
            ApiResponse<PagedResponse<UserResponse>> body = response.getBody();
            if (body == null || body.getData() == null || body.getData().getContent() == null) {
                return Collections.emptyList();
            }
            return body.getData().getContent().stream()
                    .map(UserResponse::getUserId)
                    .toList();
        } catch (Exception ex) {
            log.error("Failed to fetch followers for userId={}", userId, ex);
            return Collections.emptyList();
        }
    }

    @Data
    private static class ApiResponse<T> {
        private boolean success;
        private T data;
    }

    @Data
    private static class PagedResponse<T> {
        private List<T> content;
    }

    @Data
    @AllArgsConstructor
    private static class UserResponse {
        private Long userId;
        private String username;
    }
}

