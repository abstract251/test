package com.test.oes.async.payload;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExamRevokedPayload {
    private Integer examCode;
    private LocalDateTime revokedAt;
    private Integer adminId;
    private String reason;
    private Integer paperId;
}
