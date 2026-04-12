package com.test.oes.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

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

    /** 开考时刻（Asia/Shanghai），与评审稿 exam_start_at 对齐 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime examStartAt;

    /** 本场共用快照生成完成时刻 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime paperFrozenAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime revokedAt;

    private String revokeReason;

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