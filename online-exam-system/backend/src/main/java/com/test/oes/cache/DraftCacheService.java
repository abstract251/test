package com.test.oes.cache;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;

@Component
public class DraftCacheService {

    private static final String CACHE_NAME = "student-draft";

    private final JsonRedisStore jsonRedisStore;
    private final Optional<StringRedisTemplate> redisTemplate;

    public DraftCacheService(JsonRedisStore jsonRedisStore, Optional<StringRedisTemplate> redisTemplate) {
        this.jsonRedisStore = jsonRedisStore;
        this.redisTemplate = redisTemplate;
    }

    public StudentDraftCacheValue getDraft(Integer examCode, Integer studentId) {
        return jsonRedisStore.get(CACHE_NAME, CacheKeys.studentDraft(examCode, studentId), StudentDraftCacheValue.class);
    }

    public boolean saveDraft(Integer examCode, Integer studentId, StudentDraftCacheValue draft, Duration ttl) {
        return jsonRedisStore.set(CACHE_NAME, CacheKeys.studentDraft(examCode, studentId), draft, ttl);
    }

    public void deleteDraft(Integer examCode, Integer studentId) {
        jsonRedisStore.delete(CACHE_NAME, CacheKeys.studentDraft(examCode, studentId));
    }

    public boolean isEnabled() {
        return jsonRedisStore.isEnabled();
    }

    public boolean scheduleFlush(Integer examCode, Integer studentId, Instant dueAt) {
        if (!isEnabled()) {
            return false;
        }
        try {
            redisTemplate.orElseThrow().opsForZSet().add(
                    CacheKeys.draftFlushDue(),
                    examCode + ":" + studentId,
                    dueAt.toEpochMilli()
            );
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    public Set<String> popDueFlushCandidates(Instant now, int batchSize) {
        if (!isEnabled()) {
            return Set.of();
        }
        try {
            return redisTemplate.orElseThrow().opsForZSet()
                    .rangeByScore(CacheKeys.draftFlushDue(), 0, now.toEpochMilli(), 0, batchSize);
        } catch (Exception ignored) {
            return Set.of();
        }
    }

    public boolean removeDueFlushCandidate(Integer examCode, Integer studentId) {
        if (!isEnabled()) {
            return false;
        }
        try {
            Long removed = redisTemplate.orElseThrow().opsForZSet()
                    .remove(CacheKeys.draftFlushDue(), examCode + ":" + studentId);
            return removed != null && removed > 0;
        } catch (Exception ignored) {
            return false;
        }
    }

    public boolean tryAcquireFlushLock(Integer examCode, Integer studentId, Duration ttl) {
        if (!isEnabled()) {
            return false;
        }
        try {
            Boolean success = redisTemplate.orElseThrow().opsForValue()
                    .setIfAbsent(CacheKeys.draftFlushLock(examCode, studentId), "1", ttl);
            return Boolean.TRUE.equals(success);
        } catch (Exception ignored) {
            return false;
        }
    }

    public void releaseFlushLock(Integer examCode, Integer studentId) {
        if (!isEnabled()) {
            return;
        }
        try {
            redisTemplate.orElseThrow().delete(CacheKeys.draftFlushLock(examCode, studentId));
        } catch (Exception ignored) {
        }
    }
}
