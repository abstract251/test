package com.test.oes.cache;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ExamMetaCacheValue {
    private Integer examCode;
    private String source;
    private String description;
    private String type;
    private String tips;
    private String examDate;
    private LocalDateTime examStartAt;
    private Integer totalTime;
    private Integer totalScore;
    private String grade;
    private String major;
    private String institute;
    private LocalDateTime freezeAt;
    private LocalDateTime windowEndAt;
    private Boolean paperLocked;
    private Boolean revoked;
    private String revokeReason;
    private List<QuestionSummaryCacheItem> questionSummary;
    private Integer totalQuestionCount;
    private String summarySource;
}
