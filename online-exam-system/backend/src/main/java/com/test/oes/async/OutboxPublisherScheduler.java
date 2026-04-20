package com.test.oes.async;

import com.test.oes.entity.AsyncEventOutbox;
import com.test.oes.mapper.AsyncEventOutboxMapper;
import com.test.oes.runtime.RuntimeFeatureKey;
import com.test.oes.runtime.RuntimeToggleManager;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OutboxPublisherScheduler {

    private final AsyncProperties asyncProperties;
    private final AsyncEventOutboxMapper asyncEventOutboxMapper;
    private final AsyncEventPublisher asyncEventPublisher;
    private final AsyncMetrics asyncMetrics;
    private final TransactionTemplate transactionTemplate;
    private final RuntimeToggleManager runtimeToggleManager;

    @Scheduled(initialDelay = 3000L, fixedDelay = 2000L)
    public void publishPending() {
        try {
            asyncMetrics.updateOutboxPending(asyncEventOutboxMapper.countPending());
            if (!runtimeToggleManager.isEnabled(RuntimeFeatureKey.ASYNC_OUTBOX_PUBLISHER_ENABLED)) {
                return;
            }
            List<AsyncEventOutbox> batch = lockBatch();
            for (AsyncEventOutbox row : batch) {
                asyncMetrics.outboxPublishLatency().record(() -> publishSingle(row));
            }
            asyncMetrics.updateOutboxPending(asyncEventOutboxMapper.countPending());
        } catch (Exception ignored) {
        }
    }

    protected List<AsyncEventOutbox> lockBatch() {
        return transactionTemplate.execute(status -> {
            List<AsyncEventOutbox> batch = asyncEventOutboxMapper.lockBatchForPublish(
                    LocalDateTime.now(),
                    asyncProperties.getOutbox().getBatchSize()
            );
            if (!batch.isEmpty()) {
                asyncEventOutboxMapper.updateStatusBatch(batch.stream().map(AsyncEventOutbox::getId).toList(), "PROCESSING");
            }
            return batch;
        });
    }

    protected void publishSingle(AsyncEventOutbox row) {
        try {
            asyncEventPublisher.publish(row);
            asyncEventOutboxMapper.markPublished(row.getId(), LocalDateTime.now());
            asyncMetrics.outboxPublishTotal().increment();
        } catch (Exception exception) {
            int retryCount = (row.getRetryCount() == null ? 0 : row.getRetryCount()) + 1;
            boolean failed = retryCount >= asyncProperties.getOutbox().getMaxRetries();
            asyncEventOutboxMapper.markRetryOrFailed(
                    row.getId(),
                    failed ? "FAILED" : "RETRY",
                    retryCount,
                    failed ? null : LocalDateTime.now().plusSeconds(Math.min(60L, retryCount * 5L)),
                    exception.getMessage()
            );
        }
    }
}
