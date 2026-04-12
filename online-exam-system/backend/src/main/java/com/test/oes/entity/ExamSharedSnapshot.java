package com.test.oes.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExamSharedSnapshot {
    private Integer examCode;
    private Integer paperId;
    private LocalDateTime createdAt;
}
