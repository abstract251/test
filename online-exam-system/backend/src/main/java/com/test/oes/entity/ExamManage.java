package com.test.oes.entity;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ExamManage {
    private Integer examCode;

    private String description;

    private String source;

    private Integer paperId;

    private String examDate;

    private Integer totalTime;

    private String grade;

    private String term;

    private String major;

    private String institute;

    private Integer totalScore;

    private String type;

    private String tips;

    @Override
    public String toString() {
        return "ExamManage{" +
                "examCode=" + examCode +
                ", description='" + description + '\'' +
                ", source='" + source + '\'' +
                ", paperId=" + paperId +
                ", examDate='" + examDate + '\'' +
                ", totalTime=" + totalTime +
                ", grade='" + grade + '\'' +
                ", term='" + term + '\'' +
                ", major='" + major + '\'' +
                ", institute='" + institute + '\'' +
                ", totalScore=" + totalScore +
                ", type='" + type + '\'' +
                ", tips='" + tips + '\'' +
                '}';
    }
}