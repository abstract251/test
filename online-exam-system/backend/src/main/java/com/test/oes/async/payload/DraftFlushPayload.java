package com.test.oes.async.payload;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DraftFlushPayload {
    private Integer examCode;
    private Integer studentId;
    private Long draftRevision;
    private LocalDateTime requestedAt;
}
