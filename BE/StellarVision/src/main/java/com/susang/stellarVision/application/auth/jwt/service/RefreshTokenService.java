package com.susang.stellarVision.application.auth.jwt.service;

import java.time.Duration;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String PREFIX = "refreshToken:";

    public void store(String email, String refreshToken, Duration ttl) {
        String key = PREFIX + email;
        redisTemplate.opsForValue().set(key, refreshToken, ttl);
    }

    public String get(String email) {
        return Objects.requireNonNull(redisTemplate.opsForValue()
                        .get(PREFIX + email))
                .toString();
    }

    public void delete(String email) {
        redisTemplate.delete(PREFIX + email);
    }
}
