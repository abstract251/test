package com.test.oes.cache;

import com.test.oes.entity.AuthRefreshToken;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.function.Supplier;

@Component
public class RefreshTokenHotStateService {

    private static final String CACHE_NAME = "refresh-token";
    private static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");

    private final JsonRedisStore jsonRedisStore;
    private final CacheMetrics cacheMetrics;

    public RefreshTokenHotStateService(JsonRedisStore jsonRedisStore, CacheMetrics cacheMetrics) {
        this.jsonRedisStore = jsonRedisStore;
        this.cacheMetrics = cacheMetrics;
    }

    public RefreshTokenHotState getOrLoad(String jti, Supplier<AuthRefreshToken> loader) {
        RefreshTokenHotState hotState = jsonRedisStore.get(CACHE_NAME, CacheKeys.refreshToken(jti), RefreshTokenHotState.class);
        if (hotState != null) {
            return hotState;
        }
        AuthRefreshToken loaded = cacheMetrics.recordLoad(CACHE_NAME, "db", loader::get);
        if (loaded == null) {
            return null;
        }
        RefreshTokenHotState value = fromEntity(loaded);
        jsonRedisStore.set(CACHE_NAME, CacheKeys.refreshToken(jti), value, ttlOf(loaded.getExpiresAt()));
        return value;
    }

    public void store(AuthRefreshToken token) {
        if (token == null || token.getJti() == null) {
            return;
        }
        jsonRedisStore.set(CACHE_NAME, CacheKeys.refreshToken(token.getJti()), fromEntity(token), ttlOf(token.getExpiresAt()));
    }

    public void markRevoked(String jti, AuthRefreshToken token) {
        if (jti == null || jti.isBlank()) {
            return;
        }
        RefreshTokenHotState state = token == null ? new RefreshTokenHotState() : fromEntity(token);
        state.setRevoked(Boolean.TRUE);
        jsonRedisStore.set(CACHE_NAME, CacheKeys.refreshToken(jti), state, ttlOf(state.getExpiresAt()));
    }

    private RefreshTokenHotState fromEntity(AuthRefreshToken token) {
        RefreshTokenHotState state = new RefreshTokenHotState();
        state.setId(token.getId());
        state.setUserId(token.getUserId());
        state.setRole(token.getRole());
        state.setUsername(token.getUsername());
        state.setExpiresAt(token.getExpiresAt());
        state.setRevoked(token.getRevoked() != null && token.getRevoked() == 1);
        return state;
    }

    private Duration ttlOf(LocalDateTime expiresAt) {
        if (expiresAt == null) {
            return Duration.ofDays(7);
        }
        Duration ttl = Duration.between(LocalDateTime.now(SHANGHAI), expiresAt);
        return ttl.isNegative() || ttl.isZero() ? Duration.ofSeconds(1) : ttl;
    }
}
