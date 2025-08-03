package com.susang.stellarVision.common.redis;

import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

public interface RedisValueRepository {

    RedisTemplate<String, Object> getRedisTemplate();

    String getPrefix();

    default void save(String key, String value, Duration ttl) {
        getRedisTemplate().opsForValue()
                .set(getPrefix() + key, value, ttl);
    }

    default Optional<String> find(String key) {
        Object val = getRedisTemplate().opsForValue().get(getPrefix() + key);
        return Optional.ofNullable(val)
                .map(Object::toString);
    }

    default void delete(String key) {
        getRedisTemplate().delete(getPrefix() + key);
    }
}
