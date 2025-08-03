package com.susang.stellarVision.application.auth.email.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import static com.susang.stellarVision.common.constants.RedisKeyConstants.EMAIL_KEY_PREFIX;


@Repository
@RequiredArgsConstructor
public class RedisEmailAuthTokenRepository implements EmailAuthTokenRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public RedisTemplate<String, Object> getRedisTemplate() {
        return redisTemplate;
    }

    @Override
    public String getPrefix() {
        return EMAIL_KEY_PREFIX;
    }
}
