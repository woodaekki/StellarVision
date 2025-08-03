package com.susang.stellarVision.application.auth.email.repository;

import com.susang.stellarVision.common.redis.RedisValueRepository;

import java.time.Duration;
import java.util.Optional;

public interface EmailAuthTokenRepository extends RedisValueRepository {

    default void saveCode(String email, String code, Duration ttl) {
        RedisValueRepository.super.save(email, code, ttl);
    }

    default Optional<String> findCodeByEmail(String email) {
        return RedisValueRepository.super.find(email);
    }

    default void deleteCodeByEmail(String email) {
        RedisValueRepository.super.delete(email);
    }
}
