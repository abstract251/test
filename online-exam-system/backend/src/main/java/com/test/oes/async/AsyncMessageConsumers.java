package com.test.oes.async;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.test.oes.async.payload.AuthRefreshRevokedPayload;
import com.test.oes.async.payload.DraftFlushPayload;
import com.test.oes.async.payload.ExamRevokedPayload;
import com.test.oes.async.payload.ExamSubmittedPayload;
import com.test.oes.async.payload.MessageCreatedPayload;
import com.test.oes.cache.DraftCacheService;
import com.test.oes.cache.ExamCacheFacade;
import com.test.oes.cache.MessageCacheFacade;
import com.test.oes.cache.RefreshTokenHotStateService;
import com.test.oes.cache.StudentDraftCacheValue;
import com.test.oes.mapper.ExamAttemptMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AsyncMessageConsumers {

    private static final String CONSUMER_SUBMITTED = "score-statistics-projection";
    private static final String CONSUMER_REVOKED = "exam-revoked-invalidate";
    private static final String CONSUMER_DRAFT_FLUSH = "draft-flush-consumer";
    private static final String CONSUMER_REFRESH_REVOKED = "refresh-revoked-invalidate";
    private static final String CONSUMER_MESSAGE_CREATED = "message-created-invalidate";

    private final ObjectMapper objectMapper;
    private final AsyncProperties asyncProperties;
    private final AsyncConsumeSupport asyncConsumeSupport;
    private final ScoreStatisticsProjectionService projectionService;
    private final DraftCacheService draftCacheService;
    private final RefreshTokenHotStateService refreshTokenHotStateService;
    private final MessageCacheFacade messageCacheFacade;
    private final ExamAttemptMapper examAttemptMapper;
    private final ExamCacheFacade examCacheFacade;
    private final AsyncMetrics asyncMetrics;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = AsyncQueues.EXAM_SUBMITTED_STATISTICS)
    public void onExamSubmitted(Message message, Channel channel) throws Exception {
        process(message, channel, AsyncQueues.EXAM_SUBMITTED_STATISTICS, CONSUMER_SUBMITTED, new TypeReference<AsyncEventEnvelope<ExamSubmittedPayload>>() {
        }, envelope -> asyncMetrics.projectionRefreshLatency().record(() -> {
            projectionService.rebuildProjection(envelope.getPayload().getExamCode(), envelope.getEventId());
            asyncMetrics.consumerTotal().increment();
        }));
    }

    @RabbitListener(queues = AsyncQueues.EXAM_REVOKED_INVALIDATE)
    public void onExamRevoked(Message message, Channel channel) throws Exception {
        process(message, channel, AsyncQueues.EXAM_REVOKED_INVALIDATE, CONSUMER_REVOKED, new TypeReference<AsyncEventEnvelope<ExamRevokedPayload>>() {
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
        process(message, channel, AsyncQueues.DRAFT_FLUSH, CONSUMER_DRAFT_FLUSH, new TypeReference<AsyncEventEnvelope<DraftFlushPayload>>() {
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

    @RabbitListener(queues = AsyncQueues.AUTH_REFRESH_REVOKED_INVALIDATE)
    public void onAuthRefreshRevoked(Message message, Channel channel) throws Exception {
        process(message, channel, AsyncQueues.AUTH_REFRESH_REVOKED_INVALIDATE, CONSUMER_REFRESH_REVOKED, new TypeReference<AsyncEventEnvelope<AuthRefreshRevokedPayload>>() {
        }, envelope -> {
            AuthRefreshRevokedPayload payload = envelope.getPayload();
            if (payload == null || payload.getJti() == null || payload.getJti().isBlank()) {
                throw new IllegalStateException("Invalid auth.refresh.revoked payload");
            }
            com.test.oes.entity.AuthRefreshToken token = new com.test.oes.entity.AuthRefreshToken();
            token.setId(payload.getTokenId());
            token.setJti(payload.getJti());
            token.setUserId(payload.getUserId());
            token.setUsername(payload.getUsername());
            token.setRole(payload.getRole());
            token.setExpiresAt(payload.getExpiresAt());
            token.setRevoked(1);
            refreshTokenHotStateService.markRevoked(payload.getJti(), token);
            asyncMetrics.consumerTotal().increment();
        });
    }

    @RabbitListener(queues = AsyncQueues.MESSAGE_CREATED_INVALIDATE)
    public void onMessageCreated(Message message, Channel channel) throws Exception {
        process(message, channel, AsyncQueues.MESSAGE_CREATED_INVALIDATE, CONSUMER_MESSAGE_CREATED, new TypeReference<AsyncEventEnvelope<MessageCreatedPayload>>() {
        }, envelope -> {
            MessageCreatedPayload payload = envelope.getPayload();
            if (payload == null || payload.getMessageId() == null || payload.getMessageId() <= 0) {
                throw new IllegalStateException("Invalid message.created payload");
            }
            messageCacheFacade.bumpMessageFeedVersion();
            asyncMetrics.consumerTotal().increment();
        });
    }

    private <T> void process(Message message,
                             Channel channel,
                             String queueName,
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
            if (retryCount(message) >= asyncProperties.getConsumer().getMaxRetries()) {
                rabbitTemplate.send("", queueName + ".dlq", message);
                channel.basicAck(deliveryTag, false);
            } else {
                channel.basicReject(deliveryTag, false);
            }
            throw exception;
        }
    }

    @SuppressWarnings("unchecked")
    private long retryCount(Message message) {
        Object value = message.getMessageProperties().getHeaders().get("x-death");
        if (!(value instanceof List<?> deaths) || deaths.isEmpty()) {
            return 0L;
        }
        Object first = deaths.get(0);
        if (!(first instanceof Map<?, ?> deathMap)) {
            return 0L;
        }
        Object count = deathMap.get("count");
        return count instanceof Number number ? number.longValue() : 0L;
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
