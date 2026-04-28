package com.test.oes.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TeacherExamListItemVO {

    private Integer examCode;
    private String source;
    private String description;
    private String examDate;
    private LocalDateTime examStartAt;
    private Integer totalTime;
    private String grade;
    private String major;
    private String institute;
    private Integer totalScore;
    private Integer paperId;
    private LocalDateTime freezeAt;
    private LocalDateTime windowEndAt;
    private Boolean paperLocked;
    private Boolean revoked;
    private String revokeReason;
    private Boolean inExamWindow;
    private Boolean snapshotReady;
}
