package com.test.oes.entity;

import lombok.Getter;
import lombok.Setter;

//判断题实体类
@Setter
@Getter
public class JudgeQuestion {
    private Integer questionId;

    private String subject;

    private String question;

    private String answer;

    private String level;

    private String section;

    private Integer score;

    private String analysis; //题目解析

    @Override
    public String toString() {
        return "JudgeQuestion{" +
                "questionId=" + questionId +
                ", subject='" + subject + '\'' +
                ", question='" + question + '\'' +
                ", answer='" + answer + '\'' +
                ", level='" + level + '\'' +
                ", section='" + section + '\'' +
                ", score=" + score +
                ", analysis='" + analysis + '\'' +
                '}';
    }
}