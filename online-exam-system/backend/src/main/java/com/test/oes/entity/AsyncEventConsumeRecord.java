package com.test.oes.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AsyncEventConsumeRecord {
    private Long id;
    private String consumerName;
    private String eventId;
    private String eventType;
    private LocalDateTime processedAt;
    private String status;
    private String errorMessage;
}
