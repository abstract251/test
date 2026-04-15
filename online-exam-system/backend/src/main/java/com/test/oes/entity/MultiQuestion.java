package com.test.oes.entity;

import lombok.Getter;
import lombok.Setter;

// 选择题实体
@Setter
@Getter
public class MultiQuestion {
    private Integer questionId;

    private String subject;

    private String section;

    private String answerA;

    private String answerB;

    private String answerC;

    private String answerD;

    private String question;

    private String level;

    private String rightAnswer;

    private String analysis; //题目解析

    private Integer score;

    @Override
    public String toString() {
        return "MultiQuestion{" +
                "questionId=" + questionId +
                ", subject='" + subject + '\'' +
                ", section='" + section + '\'' +
                ", answerA='" + answerA + '\'' +
                ", answerB='" + answerB + '\'' +
                ", answerC='" + answerC + '\'' +
                ", answerD='" + answerD + '\'' +
                ", question='" + question + '\'' +
                ", level='" + level + '\'' +
                ", rightAnswer='" + rightAnswer + '\'' +
                ", analysis='" + analysis + '\'' +
                ", score=" + score +
                '}';
    }
}