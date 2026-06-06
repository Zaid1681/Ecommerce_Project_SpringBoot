package com.platform.ecommerce.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class SessionService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final long SESSION_TTL = 10; // minutes

    public void storeSession(String username, String token) {
        String key = buildKey(username);

        redisTemplate.opsForValue()
                .set(key, token, Duration.ofMinutes(SESSION_TTL));
    }

    public boolean isSessionValid(String username, String token) {
        String key = buildKey(username);

        String storedToken = (String) redisTemplate.opsForValue().get(key);

        return token != null && token.equals(storedToken);
    }

    public void deleteSession(String username) {
        redisTemplate.delete(buildKey(username));
    }

    private String buildKey(String username) {
        return "session:" + username;
    }
}
