package com.test.oes.async;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class AsyncMetrics {

    private final Counter outboxPublishTotal;
    private final Counter consumerTotal;
    private final Counter draftFlushTotal;
    private final Timer outboxPublishLatency;
    private final Timer projectionRefreshLatency;
    private final Timer draftFlushLatency;
    private final AtomicLong outboxPending = new AtomicLong();

    public AsyncMetrics(MeterRegistry registry) {
        this.outboxPublishTotal = Counter.builder("oes_async_outbox_publish_total").register(registry);
        this.consumerTotal = Counter.builder("oes_async_consumer_total").register(registry);
        this.draftFlushTotal = Counter.builder("oes_draft_flush_total").register(registry);
        this.outboxPublishLatency = Timer.builder("oes_async_outbox_publish_latency").register(registry);
        this.projectionRefreshLatency = Timer.builder("oes_async_projection_refresh_latency").register(registry);
        this.draftFlushLatency = Timer.builder("oes_draft_flush_latency").register(registry);
        Gauge.builder("oes_async_outbox_pending", outboxPending, AtomicLong::get).register(registry);
    }

    public void updateOutboxPending(long value) {
        outboxPending.set(value);
    }

    public Counter outboxPublishTotal() {
        return outboxPublishTotal;
    }

    public Counter consumerTotal() {
        return consumerTotal;
    }

    public Counter draftFlushTotal() {
        return draftFlushTotal;
    }

    public Timer outboxPublishLatency() {
        return outboxPublishLatency;
    }

    public Timer projectionRefreshLatency() {
        return projectionRefreshLatency;
    }

    public Timer draftFlushLatency() {
        return draftFlushLatency;
    }
}
