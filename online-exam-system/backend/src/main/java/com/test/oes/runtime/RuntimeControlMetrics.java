package com.test.oes.runtime;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class RuntimeControlMetrics {

    private final MeterRegistry registry;
    private final AtomicLong version = new AtomicLong();
    private final Map<RuntimeFeatureKey, AtomicLong> featureStates = new EnumMap<>(RuntimeFeatureKey.class);

    public RuntimeControlMetrics(MeterRegistry registry) {
        this.registry = registry;
        Gauge.builder("oes_runtime_toggle_version", version, AtomicLong::get).register(registry);
        for (RuntimeFeatureKey key : RuntimeFeatureKey.values()) {
            AtomicLong holder = new AtomicLong(key.defaultEnabled() ? 1L : 0L);
            featureStates.put(key, holder);
            Gauge.builder("oes_runtime_toggle_enabled", holder, AtomicLong::get)
                    .tag("feature", key.name())
                    .register(registry);
        }
    }

    public void updateVersion(long currentVersion) {
        version.set(currentVersion);
    }

    public void updateFeature(RuntimeFeatureKey key, boolean enabled) {
        featureStates.get(key).set(enabled ? 1L : 0L);
    }

    public void recordApply(String type) {
        Counter.builder("oes_runtime_toggle_apply_total")
                .tag("type", type)
                .register(registry)
                .increment();
    }

    public void recordPreset(String preset) {
        Counter.builder("oes_runtime_preset_apply_total")
                .tag("preset", preset)
                .register(registry)
                .increment();
    }
}
