package com.test.oes.runtime;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class RuntimeToggleManager {

    private final AtomicLong version = new AtomicLong(0L);
    private final AtomicReference<LocalDateTime> refreshedAt = new AtomicReference<>();
    private final EnumMap<RuntimeFeatureKey, Boolean> states = new EnumMap<>(RuntimeFeatureKey.class);
    private final RuntimeControlMetrics metrics;

    public RuntimeToggleManager(RuntimeControlMetrics metrics) {
        this.metrics = metrics;
        for (RuntimeFeatureKey key : RuntimeFeatureKey.values()) {
            states.put(key, key.defaultEnabled());
            metrics.updateFeature(key, key.defaultEnabled());
        }
        refreshedAt.set(LocalDateTime.now());
    }

    public synchronized void apply(List<RuntimeFeatureToggle> toggles) {
        EnumMap<RuntimeFeatureKey, Boolean> next = new EnumMap<>(RuntimeFeatureKey.class);
        for (RuntimeFeatureKey key : RuntimeFeatureKey.values()) {
            next.put(key, key.defaultEnabled());
        }
        long currentVersion = 0L;
        for (RuntimeFeatureToggle toggle : toggles) {
            RuntimeFeatureKey key = RuntimeFeatureKey.from(toggle.getFeatureKey());
            boolean enabled = Boolean.TRUE.equals(toggle.getEnabled());
            next.put(key, enabled);
            currentVersion = Math.max(currentVersion, toggle.getVersion() == null ? 0L : toggle.getVersion());
        }
        states.clear();
        states.putAll(next);
        version.set(currentVersion);
        refreshedAt.set(LocalDateTime.now());
        metrics.updateVersion(currentVersion);
        for (Map.Entry<RuntimeFeatureKey, Boolean> entry : next.entrySet()) {
            metrics.updateFeature(entry.getKey(), entry.getValue());
        }
    }

    public boolean isEnabled(RuntimeFeatureKey key) {
        Boolean enabled = states.get(key);
        return enabled == null ? key.defaultEnabled() : enabled;
    }

    public long currentVersion() {
        return version.get();
    }

    public LocalDateTime refreshedAt() {
        return refreshedAt.get();
    }

    public Map<String, Boolean> currentSnapshot() {
        Map<String, Boolean> snapshot = new LinkedHashMap<>();
        for (RuntimeFeatureKey key : RuntimeFeatureKey.values()) {
            snapshot.put(key.name(), isEnabled(key));
        }
        return snapshot;
    }
}
