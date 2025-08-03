package com.susang.stellarVision.application.auth.jwt.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;


import static com.susang.stellarVision.common.constants.RedisKeyConstants.JWT_KEY_PREFIX;


@Repository
@RequiredArgsConstructor
public class RedisRefreshTokenRepository implements RefreshTokenRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public RedisTemplate<String, Object> getRedisTemplate() {
        return redisTemplate;
    }

    @Override
    public String getPrefix() {
        return JWT_KEY_PREFIX;
    }
}
