package com.test.oes.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class DatabaseRouteMetrics {

    private final MeterRegistry meterRegistry;
    private final AtomicInteger replicaLagSeconds = new AtomicInteger(-1);

    public DatabaseRouteMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        Gauge.builder("oes.db.replica.lag.seconds", replicaLagSeconds, AtomicInteger::get)
                .description("Observed replica lag seconds, -1 means unavailable")
                .register(meterRegistry);
    }

    public void recordRoute(String target, String result) {
        Counter.builder("oes.db.route.requests")
                .tag("target", target)
                .tag("result", result)
                .register(meterRegistry)
                .increment();
    }

    public void updateReplicaLag(Integer seconds) {
        replicaLagSeconds.set(seconds == null ? -1 : seconds);
    }
}
