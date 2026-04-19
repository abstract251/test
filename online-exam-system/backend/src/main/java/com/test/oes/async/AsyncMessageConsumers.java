package com.test.oes.async;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.test.oes.async.payload.DraftFlushPayload;
import com.test.oes.async.payload.ExamRevokedPayload;
import com.test.oes.async.payload.ExamSubmittedPayload;
import com.test.oes.cache.DraftCacheService;
import com.test.oes.cache.ExamCacheFacade;
import com.test.oes.cache.StudentDraftCacheValue;
import com.test.oes.mapper.ExamAttemptMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AsyncMessageConsumers {

    private static final String CONSUMER_SUBMITTED = "score-statistics-projection";
    private static final String CONSUMER_REVOKED = "exam-revoked-invalidate";
    private static final String CONSUMER_DRAFT_FLUSH = "draft-flush-consumer";

    private final ObjectMapper objectMapper;
    private final AsyncConsumeSupport asyncConsumeSupport;
    private final ScoreStatisticsProjectionService projectionService;
    private final DraftCacheService draftCacheService;
    private final ExamAttemptMapper examAttemptMapper;
    private final ExamCacheFacade examCacheFacade;
    private final AsyncMetrics asyncMetrics;

    @RabbitListener(queues = AsyncQueues.EXAM_SUBMITTED_STATISTICS)
    public void onExamSubmitted(Message message, Channel channel) throws Exception {
        process(message, channel, CONSUMER_SUBMITTED, new TypeReference<AsyncEventEnvelope<ExamSubmittedPayload>>() {
        }, envelope -> asyncMetrics.projectionRefreshLatency().record(() -> {
            projectionService.rebuildProjection(envelope.getPayload().getExamCode(), envelope.getEventId());
            asyncMetrics.consumerTotal().increment();
        }));
    }

    @RabbitListener(queues = AsyncQueues.EXAM_REVOKED_INVALIDATE)
    public void onExamRevoked(Message message, Channel channel) throws Exception {
        process(message, channel, CONSUMER_REVOKED, new TypeReference<AsyncEventEnvelope<ExamRevokedPayload>>() {
        }, envelope -> {
            Integer examCode = envelope.getPayload().getExamCode();
            projectionService.deleteProjection(examCode);
            examCacheFacade.evictExamMeta(examCode);
            examCacheFacade.evictSnapshotCaches(examCode);
            examCacheFacade.evictScoreStatistics(examCode);
            asyncMetrics.consumerTotal().increment();
        });
    }

    @RabbitListener(queues = AsyncQueues.DRAFT_FLUSH)
    public void onDraftFlush(Message message, Channel channel) throws Exception {
        process(message, channel, CONSUMER_DRAFT_FLUSH, new TypeReference<AsyncEventEnvelope<DraftFlushPayload>>() {
        }, envelope -> asyncMetrics.draftFlushLatency().record(() -> {
            DraftFlushPayload payload = envelope.getPayload();
            StudentDraftCacheValue draft = draftCacheService.getDraft(payload.getExamCode(), payload.getStudentId());
            if (draft == null || draft.getAnswers() == null) {
                draftCacheService.removeDueFlushCandidate(payload.getExamCode(), payload.getStudentId());
                return;
            }
            persistAnswers(payload.getExamCode(), payload.getStudentId(), draft.getAnswers());
            draft.setLastPersistedAt(java.time.LocalDateTime.now());
            draft.setLastPersistedRevision(draft.getRevision());
            draft.setDirty(Boolean.FALSE);
            draftCacheService.saveDraft(payload.getExamCode(), payload.getStudentId(), draft, java.time.Duration.ofMinutes(30));
            draftCacheService.removeDueFlushCandidate(payload.getExamCode(), payload.getStudentId());
            asyncMetrics.draftFlushTotal().increment();
            asyncMetrics.consumerTotal().increment();
        }));
    }

    private <T> void process(Message message,
                             Channel channel,
                             String consumerName,
                             TypeReference<AsyncEventEnvelope<T>> type,
                             ThrowingConsumer<AsyncEventEnvelope<T>> handler) throws Exception {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        AsyncEventEnvelope<T> envelope = objectMapper.readValue(
                new String(message.getBody(), StandardCharsets.UTF_8),
                type
        );
        if (!asyncConsumeSupport.tryStart(consumerName, envelope.getEventId(), envelope.getEventType())) {
            channel.basicAck(deliveryTag, false);
            return;
        }
        try {
            handler.accept(envelope);
            asyncConsumeSupport.markSuccess(consumerName, envelope.getEventId());
            channel.basicAck(deliveryTag, false);
        } catch (Exception exception) {
            asyncConsumeSupport.markFailed(consumerName, envelope.getEventId(), exception.getMessage());
            channel.basicReject(deliveryTag, false);
            throw exception;
        }
    }

    private void persistAnswers(Integer examCode, Integer studentId, Map<String, String> answers) {
        var attempt = examAttemptMapper.findByExamAndStudent(examCode, studentId);
        if (attempt == null) {
            return;
        }
        try {
            examAttemptMapper.updateAnswers(attempt.getAttemptId(), objectMapper.writeValueAsString(answers));
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to persist draft answers", exception);
        }
    }

    @FunctionalInterface
    private interface ThrowingConsumer<T> {
        void accept(T value) throws Exception;
    }
}
