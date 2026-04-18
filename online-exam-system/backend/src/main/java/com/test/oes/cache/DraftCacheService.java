package com.test.oes.cache;

import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class DraftCacheService {

    private static final String CACHE_NAME = "student-draft";

    private final JsonRedisStore jsonRedisStore;

    public DraftCacheService(JsonRedisStore jsonRedisStore) {
        this.jsonRedisStore = jsonRedisStore;
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
}
