package com.test.oes.observability;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class ObservabilityConfig {

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> meterRegistryCustomizer() {
        return registry -> registry.config().commonTags(
                "system", "online-exam-system",
                "service", "backend"
        );
    }

    @Bean
    public MeterFilter endpointLatencyDistribution() {
        return new MeterFilter() {
            @Override
            public DistributionStatisticConfig configure(io.micrometer.core.instrument.Meter.Id id,
                                                         DistributionStatisticConfig config) {
                if (!"oes.endpoint.latency".equals(id.getName())) {
                    return config;
                }
                return DistributionStatisticConfig.builder()
                        .percentilesHistogram(true)
                        .serviceLevelObjectives(
                                Duration.ofMillis(100).toNanos(),
                                Duration.ofMillis(300).toNanos(),
                                Duration.ofMillis(500).toNanos(),
                                Duration.ofSeconds(1).toNanos(),
                                Duration.ofSeconds(3).toNanos()
                        )
                        .build()
                        .merge(config);
            }
        };
    }
}
