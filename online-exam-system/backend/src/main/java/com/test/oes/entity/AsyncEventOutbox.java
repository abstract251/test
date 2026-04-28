package com.test.oes.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AsyncEventOutbox {
    private Long id;
    private String eventId;
    private String eventType;
    private String aggregateType;
    private String aggregateId;
    private String routingKey;
    private String payloadJson;
    private String status;
    private Integer retryCount;
    private LocalDateTime nextRetryAt;
    private LocalDateTime publishedAt;
    private String lastError;
    private LocalDateTime createdAt;
}
