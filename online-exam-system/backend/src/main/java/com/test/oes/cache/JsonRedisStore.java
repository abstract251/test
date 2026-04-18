package com.test.oes.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
public class JsonRedisStore {

    private final CacheProperties cacheProperties;
    private final ObjectMapper objectMapper;
    private final Optional<StringRedisTemplate> redisTemplate;
    private final CacheMetrics cacheMetrics;

    public JsonRedisStore(CacheProperties cacheProperties,
                          ObjectMapper objectMapper,
                          Optional<StringRedisTemplate> redisTemplate,
                          CacheMetrics cacheMetrics) {
        this.cacheProperties = cacheProperties;
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
        this.cacheMetrics = cacheMetrics;
    }

    public boolean isEnabled() {
        return cacheProperties.isEnabled() && cacheProperties.getRedis().isEnabled() && redisTemplate.isPresent();
    }

    public boolean isRequired() {
        return cacheProperties.getRedis().isRequired();
    }

    public <T> T get(String cacheName, String key, Class<T> type) {
        return getInternal(cacheName, key, () -> objectMapper.readValue(requiredValue(key), type));
    }

    public <T> T get(String cacheName, String key, TypeReference<T> typeReference) {
        return getInternal(cacheName, key, () -> objectMapper.readValue(requiredValue(key), typeReference));
    }

    public boolean set(String cacheName, String key, Object value, Duration ttl) {
        if (!isEnabled()) {
            return false;
        }
        try {
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.orElseThrow().opsForValue().set(key, json, ttl);
            cacheMetrics.recordRequest(cacheName, "redis", "write");
            return true;
        } catch (Exception exception) {
            handleRedisException("set", exception);
            return false;
        }
    }

    public boolean delete(String cacheName, String key) {
        if (!isEnabled()) {
            return false;
        }
        try {
            redisTemplate.orElseThrow().delete(key);
            cacheMetrics.recordRequest(cacheName, "redis", "delete");
            return true;
        } catch (Exception exception) {
            handleRedisException("delete", exception);
            return false;
        }
    }

    public long increment(String cacheName, String key) {
        if (!isEnabled()) {
            return 0L;
        }
        try {
            Long result = redisTemplate.orElseThrow().opsForValue().increment(key);
            cacheMetrics.recordRequest(cacheName, "redis", "write");
            return result == null ? 0L : result;
        } catch (Exception exception) {
            handleRedisException("increment", exception);
            return 0L;
        }
    }

    @Nullable
    public String getRaw(String cacheName, String key) {
        if (!isEnabled()) {
            return null;
        }
        try {
            String value = redisTemplate.orElseThrow().opsForValue().get(key);
            cacheMetrics.recordRequest(cacheName, "redis", value == null ? "miss" : "hit");
            return value;
        } catch (Exception exception) {
            handleRedisException("get", exception);
            return null;
        }
    }

    private <T> T getInternal(String cacheName, String key, RedisReader<T> reader) {
        if (!isEnabled()) {
            return null;
        }
        try {
            String raw = redisTemplate.orElseThrow().opsForValue().get(key);
            if (raw == null || raw.isBlank()) {
                cacheMetrics.recordRequest(cacheName, "redis", "miss");
                return null;
            }
            currentValue.set(raw);
            cacheMetrics.recordRequest(cacheName, "redis", "hit");
            return reader.read();
        } catch (Exception exception) {
            handleRedisException("get", exception);
            return null;
        } finally {
            currentValue.remove();
        }
    }

    private final ThreadLocal<String> currentValue = new ThreadLocal<>();

    private String requiredValue(String key) {
        String value = currentValue.get();
        if (value == null) {
            throw new IllegalStateException("No redis value loaded for key " + key);
        }
        return value;
    }

    private void handleRedisException(String operation, Exception exception) {
        cacheMetrics.recordRedisError(operation);
        if (cacheProperties.getRedis().isRequired()) {
            if (exception instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new IllegalStateException("Redis operation failed", exception);
        }
        if (!(exception instanceof DataAccessException)) {
            cacheMetrics.recordFallback("redis_optional_" + operation);
        }
    }

    @FunctionalInterface
    private interface RedisReader<T> {
        T read() throws Exception;
    }
}
