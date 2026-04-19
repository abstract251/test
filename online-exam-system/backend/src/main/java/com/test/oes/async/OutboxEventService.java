package com.test.oes.async;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.oes.entity.AsyncEventOutbox;
import com.test.oes.mapper.AsyncEventOutboxMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class OutboxEventService {

    private final AsyncEventOutboxMapper asyncEventOutboxMapper;
    private final ObjectMapper objectMapper;

    public String append(String eventType,
                         String aggregateType,
                         String aggregateId,
                         String routingKey,
                         Object envelope) {
        try {
            String eventId = UUID.randomUUID().toString();
            if (envelope instanceof AsyncEventEnvelope<?> asyncEventEnvelope) {
                asyncEventEnvelope.setEventId(eventId);
            }
            AsyncEventOutbox row = new AsyncEventOutbox();
            row.setEventId(eventId);
            row.setEventType(eventType);
            row.setAggregateType(aggregateType);
            row.setAggregateId(aggregateId);
            row.setRoutingKey(routingKey);
            row.setPayloadJson(objectMapper.writeValueAsString(envelope));
            row.setStatus("NEW");
            row.setRetryCount(0);
            row.setCreatedAt(LocalDateTime.now());
            asyncEventOutboxMapper.insert(row);
            return row.getEventId();
        } catch (Exception exception) {
            log.warn("Skip outbox append because phase 6 async tables are unavailable or write failed: {}", exception.getMessage());
            return null;
        }
    }
}
