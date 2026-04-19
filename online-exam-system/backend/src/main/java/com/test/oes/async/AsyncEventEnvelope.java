package com.test.oes.async;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AsyncEventEnvelope<T> {
    private String eventId;
    private String eventType;
    private String aggregateType;
    private String aggregateId;
    private LocalDateTime occurredAt;
    private Integer payloadVersion;
    private T payload;
}
