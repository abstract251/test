package com.test.oes.async;

import com.test.oes.async.payload.DraftFlushPayload;
import com.test.oes.cache.DraftCacheService;
import com.test.oes.cache.StudentDraftCacheValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DraftFlushDispatchScheduler {

    private final DraftCacheService draftCacheService;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final AsyncProperties asyncProperties;

    @Scheduled(initialDelay = 3000L, fixedDelay = 1000L)
    public void dispatchDueDrafts() {
        Set<String> due = draftCacheService.popDueFlushCandidates(java.time.Instant.now(), asyncProperties.getDraft().getDispatchBatchSize());
        for (String item : due) {
            String[] parts = item.split(":");
            if (parts.length != 2) {
                continue;
            }
            Integer examCode = Integer.parseInt(parts[0]);
            Integer studentId = Integer.parseInt(parts[1]);
            if (!draftCacheService.tryAcquireFlushLock(examCode, studentId, java.time.Duration.ofSeconds(10))) {
                continue;
            }
            try {
                StudentDraftCacheValue draft = draftCacheService.getDraft(examCode, studentId);
                if (draft == null || !Boolean.TRUE.equals(draft.getDirty())) {
                    draftCacheService.removeDueFlushCandidate(examCode, studentId);
                    continue;
                }
                DraftFlushPayload payload = new DraftFlushPayload();
                payload.setExamCode(examCode);
                payload.setStudentId(studentId);
                payload.setDraftRevision(draft.getRevision());
                payload.setRequestedAt(LocalDateTime.now(ZoneId.of("Asia/Shanghai")));
                AsyncEventEnvelope<DraftFlushPayload> envelope = new AsyncEventEnvelope<>();
                envelope.setEventId(java.util.UUID.randomUUID().toString());
                envelope.setEventType(AsyncEventTypes.DRAFT_FLUSH);
                envelope.setAggregateType("examAttempt");
                envelope.setAggregateId(examCode + ":" + studentId);
                envelope.setOccurredAt(LocalDateTime.now(ZoneId.of("Asia/Shanghai")));
                envelope.setPayloadVersion(1);
                envelope.setPayload(payload);
                rabbitTemplate.convertAndSend(
                        RabbitTopologyProperties.EXAM_COMMANDS_EXCHANGE,
                        AsyncRoutingKeys.DRAFT_FLUSH,
                        objectMapper.writeValueAsString(envelope)
                );
            } catch (Exception ignored) {
            } finally {
                draftCacheService.releaseFlushLock(examCode, studentId);
            }
        }
    }
}
