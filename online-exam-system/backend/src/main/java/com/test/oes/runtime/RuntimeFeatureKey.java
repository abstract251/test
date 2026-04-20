package com.test.oes.runtime;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum RuntimeFeatureKey {
    READ_REPLICA_ENABLED(true),
    REDIS_SHARED_CACHE_ENABLED(true),
    REDIS_DRAFT_ENABLED(true),
    ASYNC_OUTBOX_PUBLISHER_ENABLED(true),
    ASYNC_EXAM_SUBMITTED_CONSUMER_ENABLED(true),
    ASYNC_EXAM_REVOKED_CONSUMER_ENABLED(true),
    ASYNC_DRAFT_FLUSH_CONSUMER_ENABLED(true),
    ASYNC_AUTH_REFRESH_REVOKED_CONSUMER_ENABLED(true),
    ASYNC_MESSAGE_CREATED_CONSUMER_ENABLED(true),
    NON_CORE_QUESTION_BANK_ENABLED(true),
    NON_CORE_MESSAGE_ENABLED(true),
    NON_CORE_PRACTICE_ENABLED(true);

    private static final Map<String, RuntimeFeatureKey> INDEX = Arrays.stream(values())
            .collect(Collectors.toMap(Enum::name, key -> key));

    private final boolean defaultEnabled;

    RuntimeFeatureKey(boolean defaultEnabled) {
        this.defaultEnabled = defaultEnabled;
    }

    public boolean defaultEnabled() {
        return defaultEnabled;
    }

    public static RuntimeFeatureKey from(String raw) {
        RuntimeFeatureKey key = INDEX.get(raw);
        if (key == null) {
            throw new IllegalArgumentException("Unknown runtime feature key: " + raw);
        }
        return key;
    }
}
