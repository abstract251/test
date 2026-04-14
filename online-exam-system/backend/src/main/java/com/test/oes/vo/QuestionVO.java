package com.test.oes.vo;

import com.test.oes.entity.FillQuestion;
import com.test.oes.entity.JudgeQuestion;
import com.test.oes.entity.MultiQuestion;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class QuestionVO {
    private String type;

    private FillQuestion fillQuestion;

    private JudgeQuestion judgeQuestion;

    private MultiQuestion multiQuestion;

}