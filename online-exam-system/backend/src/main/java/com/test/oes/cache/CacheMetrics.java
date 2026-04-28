package com.test.oes.cache;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;

@Component
public class CacheMetrics {

    private final MeterRegistry meterRegistry;

    public CacheMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordRequest(String cache, String layer, String result) {
        Counter.builder("oes.cache.requests")
                .tag("cache", cache)
                .tag("layer", layer)
                .tag("result", result)
                .register(meterRegistry)
                .increment();
    }

    public void recordFallback(String scenario) {
        Counter.builder("oes.cache.fallbacks")
                .tag("scenario", scenario)
                .register(meterRegistry)
                .increment();
    }

    public void recordRedisError(String operation) {
        Counter.builder("oes.cache.redis.errors")
                .tag("operation", operation)
                .register(meterRegistry)
                .increment();
    }

    public <T> T recordLoad(String cache, String source, Callable<T> callable) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            return callable.call();
        } catch (RuntimeException runtimeException) {
            throw runtimeException;
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        } finally {
            sample.stop(Timer.builder("oes.cache.load.latency")
                    .tag("cache", cache)
                    .tag("source", source)
                    .register(meterRegistry));
        }
    }
}
