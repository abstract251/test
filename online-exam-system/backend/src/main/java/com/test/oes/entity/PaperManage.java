package com.test.oes.entity;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PaperManage {
    private Integer paperId;

    private Integer questionType;

    private Integer questionId;

    public PaperManage() {
    }

    public PaperManage(Integer paperId, Integer questionType, Integer questionId) {
        this.paperId = paperId;
        this.questionType = questionType;
        this.questionId = questionId;
    }

    @Override
    public String toString() {
        return "PaperManage{" +
                "paperId=" + paperId +
                ", questionType=" + questionType +
                ", questionId=" + questionId +
                '}';
    }
}