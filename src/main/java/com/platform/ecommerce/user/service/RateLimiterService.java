package com.platform.ecommerce.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimiterService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final int LIMIT = 100;
    private static final int WINDOW = 30; // seconds

    public boolean isAllowed(String username) {
        String key = "rate:" + username;

        Long count = redisTemplate.opsForValue().increment(key);

        if (count != null && count == 1) {
            redisTemplate.expire(key, Duration.ofMinutes(WINDOW));
        }

        return count != null && count <= LIMIT;
    }
}