package com.test.oes.service;

import com.test.oes.entity.PaperManage;

import java.util.List;
import java.util.Map;

public interface PaperService {

    List<PaperManage> findAll();

    List<PaperManage> findById(Integer paperId);

    int add(PaperManage paperManage);

    int addBatch(List<PaperManage> paperManages);

    // 获取试卷总分
    Integer getMaxScore(Integer paperId);

    Map<Integer, Integer> getMaxScores(List<Integer> paperIds);

    List<Integer> summarizeQuestionTypes(Integer paperId);

    /**
     * 删除试卷中的某条试题
     *
     * @param paperId 试卷id
     * @param type 题目类型。1选择，2填空，3判断
     * @param questionId 题目id
     */
    int delete(Integer paperId, Integer type, Integer questionId);

    /**
     * 根据试卷id删除题目关联
     *
     * @param paperId 试卷id
     */
    int deleteByPaperId(Integer paperId);
}
