package com.test.oes.service;

import com.test.oes.entity.Score;
import com.test.oes.vo.AnswerVO;

public interface AnswerService {
    /**
     * 提交答案并自动判分
     * @param vo 提交的答案数据
     * @return 保存后的成绩记录
     * @throws RuntimeException 当重复提交或考试/试卷不存在时抛出
     */
    Score submitAnswer(AnswerVO vo);
}
