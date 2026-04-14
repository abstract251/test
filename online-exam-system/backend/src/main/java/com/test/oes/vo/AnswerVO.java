package com.test.oes.vo;

import lombok.Data;
import java.util.List;

/**
 * 提交答案的请求体
 */
@Data
public class AnswerVO {
    private Integer examCode;          // 考试编号
    private Integer studentId;         // 学生ID
    private List<AnswerDetail> answers; // 答案列表

    @Data
    public static class AnswerDetail {
        private Integer questionType;   // 题目类型：1-选择题，2-填空题，3-判断题
        private Integer questionId;     // 题目ID
        private String answer;          // 学生答案（选择题为"A"/"B"等，填空题填文本，判断题填"T"/"F"）
    }
}