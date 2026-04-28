package com.test.oes.runtime;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

public enum RuntimeFeaturePreset {
    NORMAL(mapOf()),
    PRIMARY_ONLY(mapOf(RuntimeFeatureKey.READ_REPLICA_ENABLED, false)),
    REDIS_DEGRADED(mapOf(
            RuntimeFeatureKey.REDIS_SHARED_CACHE_ENABLED, false,
            RuntimeFeatureKey.REDIS_DRAFT_ENABLED, false
    )),
    MQ_DEGRADED(mapOf(
            RuntimeFeatureKey.ASYNC_OUTBOX_PUBLISHER_ENABLED, false,
            RuntimeFeatureKey.ASYNC_EXAM_SUBMITTED_CONSUMER_ENABLED, false,
            RuntimeFeatureKey.ASYNC_EXAM_REVOKED_CONSUMER_ENABLED, false,
            RuntimeFeatureKey.ASYNC_DRAFT_FLUSH_CONSUMER_ENABLED, false,
            RuntimeFeatureKey.ASYNC_AUTH_REFRESH_REVOKED_CONSUMER_ENABLED, false,
            RuntimeFeatureKey.ASYNC_MESSAGE_CREATED_CONSUMER_ENABLED, false
    )),
    CORE_EXAM_ONLY(mapOf(
            RuntimeFeatureKey.NON_CORE_QUESTION_BANK_ENABLED, false,
            RuntimeFeatureKey.NON_CORE_MESSAGE_ENABLED, false,
            RuntimeFeatureKey.NON_CORE_PRACTICE_ENABLED, false
    ));

    private static final Map<String, RuntimeFeaturePreset> INDEX = Arrays.stream(values())
            .collect(Collectors.toMap(Enum::name, preset -> preset));

    private final Map<RuntimeFeatureKey, Boolean> overrides;

    RuntimeFeaturePreset(Map<RuntimeFeatureKey, Boolean> overrides) {
        this.overrides = overrides;
    }

    public Map<RuntimeFeatureKey, Boolean> resolveState() {
        EnumMap<RuntimeFeatureKey, Boolean> state = new EnumMap<>(RuntimeFeatureKey.class);
        for (RuntimeFeatureKey key : RuntimeFeatureKey.values()) {
            state.put(key, overrides.getOrDefault(key, key.defaultEnabled()));
        }
        return state;
    }

    public static RuntimeFeaturePreset from(String raw) {
        RuntimeFeaturePreset preset = INDEX.get(raw);
        if (preset == null) {
            throw new IllegalArgumentException("Unknown runtime preset: " + raw);
        }
        return preset;
    }

    @SuppressWarnings("unchecked")
    private static Map<RuntimeFeatureKey, Boolean> mapOf(Object... entries) {
        EnumMap<RuntimeFeatureKey, Boolean> map = new EnumMap<>(RuntimeFeatureKey.class);
        for (int i = 0; i < entries.length; i += 2) {
            map.put((RuntimeFeatureKey) entries[i], (Boolean) entries[i + 1]);
        }
        return map;
    }
}
