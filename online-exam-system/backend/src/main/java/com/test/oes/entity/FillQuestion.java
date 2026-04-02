package com.test.oes.entity;

import lombok.Getter;
import lombok.Setter;

//填空题实体类
@Setter
@Getter
public class FillQuestion {
    private Integer questionId;

    private String subject;

    private String question;

    private String answer;

    private Integer score;

    private String level;

    private String section;

    private String analysis; //题目解析

    @Override
    public String toString() {
        return "FillQuestion{" +
                "questionId=" + questionId +
                ", subject='" + subject + '\'' +
                ", question='" + question + '\'' +
                ", answer='" + answer + '\'' +
                ", score=" + score +
                ", level='" + level + '\'' +
                ", section='" + section + '\'' +
                ", analysis='" + analysis + '\'' +
                '}';
    }
}
