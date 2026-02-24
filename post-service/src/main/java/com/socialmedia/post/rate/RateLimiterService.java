package com.socialmedia.post.rate;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RateLimiterService {

    private final StringRedisTemplate redisTemplate;

    @Value("${rate-limit.create-post.limit:10}")
    private long limit;

    @Value("${rate-limit.create-post.window-seconds:60}")
    private long windowSeconds;

    public boolean tryConsume(String key) {
        String redisKey = "rate:post:" + key;
        try {
            Long count = redisTemplate.opsForValue().increment(redisKey);
            if (count != null && count == 1L) {
                redisTemplate.expire(redisKey, Duration.ofSeconds(windowSeconds));
            }
            return count != null && count <= limit;
        } catch (DataAccessException ex) {
            return true;
        }
    }
}

