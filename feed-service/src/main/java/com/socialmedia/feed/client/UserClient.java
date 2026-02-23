package com.socialmedia.feed.client;

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
public class UserClient {

    private final RestTemplate restTemplate;

    @Value("${services.user.base-url}")
    private String userBaseUrl;

    public UserProfileResponse fetchUser(Long userId) {
        String url = userBaseUrl + "/api/users/" + userId;
        try {
            ResponseEntity<ApiResponse<UserProfileResponse>> response = restTemplate.exchange(url, HttpMethod.GET,
                    null, new ParameterizedTypeReference<ApiResponse<UserProfileResponse>>() {
                    }
            );
            ApiResponse<UserProfileResponse> body = response.getBody();
            if (body == null || body.getData() == null) {
                return null;
            }
            return body.getData();
        } catch (Exception ex) {
            log.error("Failed to fetch user profile for userId={}", userId, ex);
            return null;
        }
    }

    @Data
    private static class ApiResponse<T> {
        private boolean success;
        private T data;
    }

    @Data
    public static class UserProfileResponse {
        private Long id;
        private String username;
    }
}

