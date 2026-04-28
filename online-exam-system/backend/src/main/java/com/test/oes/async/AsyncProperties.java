package com.test.oes.async;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.async")
public class AsyncProperties {

    private Outbox outbox = new Outbox();
    private Draft draft = new Draft();
    private Consumer consumer = new Consumer();

    @Getter
    @Setter
    public static class Outbox {
        private Duration pollInterval = Duration.ofSeconds(2);
        private int batchSize = 50;
        private int maxRetries = 8;
    }

    @Getter
    @Setter
    public static class Draft {
        private Duration flushDelay = Duration.ofSeconds(5);
        private int dispatchBatchSize = 100;
    }

    @Getter
    @Setter
    public static class Consumer {
        private int maxRetries = 3;
    }
}
