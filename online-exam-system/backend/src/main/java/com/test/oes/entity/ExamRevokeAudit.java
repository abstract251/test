package com.test.oes.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExamRevokeAudit {
    private Long id;
    private Integer examCode;
    private Integer adminId;
    private String adminName;
    private String reason;
    private String detail;
    private LocalDateTime createdAt;
}
