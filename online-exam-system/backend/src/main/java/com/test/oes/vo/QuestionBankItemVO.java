package com.test.oes.vo;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class QuestionBankItemVO {
    private Integer questionType;
    private String questionTypeName;
    private Integer questionId;
    private String subject;
    private String question;
    private String section;
    private String level;
    private Integer score;
    private String analysis;
    private String answer;
    private String answerA;
    private String answerB;
    private String answerC;
    private String answerD;
}
