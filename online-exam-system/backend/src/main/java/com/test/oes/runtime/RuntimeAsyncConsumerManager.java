package com.test.oes.runtime;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class RuntimeAsyncConsumerManager {

    private static final Map<RuntimeFeatureKey, String> LISTENER_IDS = Map.of(
            RuntimeFeatureKey.ASYNC_EXAM_SUBMITTED_CONSUMER_ENABLED, "runtime-exam-submitted",
            RuntimeFeatureKey.ASYNC_EXAM_REVOKED_CONSUMER_ENABLED, "runtime-exam-revoked",
            RuntimeFeatureKey.ASYNC_DRAFT_FLUSH_CONSUMER_ENABLED, "runtime-draft-flush",
            RuntimeFeatureKey.ASYNC_AUTH_REFRESH_REVOKED_CONSUMER_ENABLED, "runtime-auth-refresh-revoked",
            RuntimeFeatureKey.ASYNC_MESSAGE_CREATED_CONSUMER_ENABLED, "runtime-message-created"
    );

    private final RabbitListenerEndpointRegistry registry;
    private final RuntimeToggleManager runtimeToggleManager;

    @Scheduled(initialDelay = 2000L, fixedDelay = 3000L)
    public void sync() {
        for (Map.Entry<RuntimeFeatureKey, String> entry : LISTENER_IDS.entrySet()) {
            MessageListenerContainer container = registry.getListenerContainer(entry.getValue());
            if (container == null) {
                continue;
            }
            boolean shouldRun = runtimeToggleManager.isEnabled(entry.getKey());
            if (shouldRun && !container.isRunning()) {
                container.start();
            } else if (!shouldRun && container.isRunning()) {
                container.stop();
            }
        }
    }
}
