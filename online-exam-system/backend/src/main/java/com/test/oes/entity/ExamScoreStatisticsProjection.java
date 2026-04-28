package com.test.oes.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExamScoreStatisticsProjection {
    private Integer examCode;
    private Double avgScore;
    private Integer maxScore;
    private Integer minScore;
    private Double passRate;
    private Integer totalCount;
    private Integer passCount;
    private String distributionJson;
    private String lastEventId;
    private LocalDateTime updatedAt;
}
