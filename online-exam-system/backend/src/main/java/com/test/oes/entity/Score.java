package com.test.oes.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.Data;

@Setter
@Getter
public class Score {
    private Integer scoreId;      // 分数编号
    private Integer examCode;     // 考试编号
    private int studentId;        // 学号
    private String subject;       // 课程名称
    private Integer ptScore;      // 是否及格 (0: 不及格, 1: 及格)
    private Integer etScore;      // 学生成绩
    private Integer score;        // 试卷满分
    private String answerDate;    // 答题日期

    @Override
    public String toString() {
        return "Score{" +
                "scoreId=" + scoreId +
                ", examCode=" + examCode +
                ", studentId=" + studentId +
                ", subject='" + subject + '\'' +
                ", ptScore=" + ptScore +
                ", etScore=" + etScore +
                ", score=" + score +
                ", answerDate='" + answerDate + '\'' +
                '}';
    }
}
