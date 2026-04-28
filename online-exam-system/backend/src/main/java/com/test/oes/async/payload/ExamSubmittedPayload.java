package com.test.oes.async.payload;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExamSubmittedPayload {
    private Integer examCode;
    private Integer studentId;
    private Long attemptId;
    private Integer scoreId;
    private LocalDateTime submittedAt;
    private Integer etScore;
    private Integer maxScore;
    private Boolean passed;
    private Integer totalQuestions;
}
