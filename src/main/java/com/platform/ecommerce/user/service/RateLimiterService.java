package com.platform.ecommerce.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimiterService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final int LIMIT = 20;
    private static final int WINDOW = 10; // seconds
//    Withing Widow duration only LIMIT number of times a user can mak a hit

    public boolean isAllowed(String username) {
        String key = "rate:" + username;

        Long count = redisTemplate.opsForValue().increment(key);
        System.out.println("IsAllowed" + count);

        if (count != null && count == 1) {
            redisTemplate.expire(key, Duration.ofMinutes(WINDOW));
        }

        return count != null && count <= LIMIT;
    }
}