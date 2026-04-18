package com.test.oes.service.impl;

import com.test.oes.entity.FillQuestion;
import com.test.oes.entity.JudgeQuestion;
import com.test.oes.entity.MultiQuestion;
import com.test.oes.entity.PaperManage;
import com.test.oes.mapper.PaperMapper;
import com.test.oes.service.*;
import com.test.oes.service.exam.ExamPaperEditPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaperServiceImpl implements PaperService {

    private final PaperMapper paperMapper;

    private final JudgeQuestionService judgeQuestionService;

    private final MultiQuestionService multiQuestionService;

    private final FillQuestionService fillQuestionService;

    private final ExamPaperEditPolicy examPaperEditPolicy;

    // 查询所有试卷
    @Override
    public List<PaperManage> findAll() {
        return paperMapper.findAll();
    }

    // 根据试卷编号查询
    @Override
    public List<PaperManage> findById(Integer paperId) {
        return paperMapper.findById(paperId);
    }

    // 添加试卷
    @Override
    public int add(PaperManage paperManage) {
        examPaperEditPolicy.assertPaperEditable(paperManage.getPaperId());
        return paperMapper.add(paperManage);
    }

    @Override
    public int addBatch(List<PaperManage> paperManages) {
        if (paperManages == null || paperManages.isEmpty()) {
            return 0;
        }
        examPaperEditPolicy.assertPaperEditable(paperManages.get(0).getPaperId());
        return paperMapper.batchInsert(paperManages);
    }

    // 计算试卷总分：每题 2 分
    @Override
    public Integer getMaxScore(Integer paperId) {
        if (paperId == null) {
            return 0;
        }
        Integer questionCount = paperMapper.countByPaperId(paperId);
        return (questionCount == null ? 0 : questionCount) * 2;
    }

    @Override
    public Map<Integer, Integer> getMaxScores(List<Integer> paperIds) {
        if (paperIds == null || paperIds.isEmpty()) {
            return Map.of();
        }
        Map<Integer, Integer> totalScores = new HashMap<>();
        for (Map<String, Object> row : paperMapper.countQuestionsByPaperIds(paperIds)) {
            Integer paperId = toInteger(row.get("paperId"));
            Integer questionCount = toInteger(row.get("questionCount"));
            if (paperId != null) {
                totalScores.put(paperId, (questionCount == null ? 0 : questionCount) * 2);
            }
        }
        return totalScores;
    }

    @Override
    public List<Integer> summarizeQuestionTypes(Integer paperId) {
        int[] counts = new int[]{0, 0, 0};
        if (paperId == null) {
            return List.of(0, 0, 0);
        }
        for (Map<String, Object> row : paperMapper.countQuestionsGroupedByType(paperId)) {
            Integer questionType = toInteger(row.get("questionType"));
            Integer questionCount = toInteger(row.get("questionCount"));
            int index = questionType == null ? -1 : questionType - 1;
            if (index >= 0 && index < counts.length) {
                counts[index] = questionCount == null ? 0 : questionCount;
            }
        }
        return List.of(counts[0], counts[1], counts[2]);
    }

    // 删除试卷中的单条试题关联
    @Override
    public int delete(Integer paperId, Integer type, Integer questionId) {
        examPaperEditPolicy.assertPaperEditable(paperId);
        return paperMapper.delete(paperId, type, questionId);
    }

    // 根据试卷ID删除所有题目关联
    @Override
    public int deleteByPaperId(Integer paperId) {
        examPaperEditPolicy.assertPaperEditable(paperId);
        return paperMapper.deleteByPaperId(paperId);
    }

    private Integer toInteger(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value == null) {
            return null;
        }
        return Integer.parseInt(String.valueOf(value));
    }
}
