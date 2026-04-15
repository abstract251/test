package com.test.oes.entity;

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

    /** 开考时刻（库中按上海业务语义存本地时间）。JSON 请用 ISO-8601，如 {@code 2026-04-22T02:00:00}（勿与 {@code yyyy-MM-dd HH:mm:ss} 混用）。 */
    private LocalDateTime examStartAt;

    /** 本场共用快照生成完成时刻 */
    private LocalDateTime paperFrozenAt;

    private LocalDateTime revokedAt;

    private String revokeReason;

    /**
     * 仅接口输出：当前是否已过「试卷冻结」时刻（不入库，由服务层填充）。
     */
    private Boolean paperLocked;

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