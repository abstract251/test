package com.test.oes.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.Data;

//题目模型
@Setter
@Getter
@Data

public class Item {

    private String subject;

    private Integer paperId;

    private Integer changeNumber;

    private Integer fillNumber;

    private Integer judgeNumber;

}