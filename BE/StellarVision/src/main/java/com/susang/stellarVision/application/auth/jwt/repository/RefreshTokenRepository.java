package com.susang.stellarVision.application.auth.jwt.repository;

import com.susang.stellarVision.common.redis.RedisValueRepository;

import java.time.Duration;
import java.util.Optional;

public interface RefreshTokenRepository extends RedisValueRepository {
    default void save(String email, String refreshToken, Duration ttl) {
        RedisValueRepository.super.save(email, refreshToken, ttl);
    }

    default Optional<String> findByEmail(String email) {
        return RedisValueRepository.super.find(email);
    }

    default void deleteByEmail(String email) {
        RedisValueRepository.super.delete(email);
    }
}
