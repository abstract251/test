package com.test.oes.cache;

import lombok.Data;

@Data
public class QuestionSummaryCacheItem {
    private Integer questionType;
    private String label;
    private Integer count;
    private Integer score;
}
