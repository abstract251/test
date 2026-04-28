package com.test.oes.async.payload;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageCreatedPayload {
    private Integer messageId;
    private String title;
    private Integer creatorId;
    private String creatorRole;
    private String creatorName;
    private LocalDateTime createdAt;
}
