package com.test.oes.service.impl;

import com.test.oes.cache.ExamCacheFacade;
import com.test.oes.entity.FillQuestion;
import com.test.oes.entity.JudgeQuestion;
import com.test.oes.entity.MultiQuestion;
import com.test.oes.entity.PaperManage;
import com.test.oes.mapper.ExamManageMapper;
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
    private final ExamManageMapper examManageMapper;

    private final JudgeQuestionService judgeQuestionService;

    private final MultiQuestionService multiQuestionService;

    private final FillQuestionService fillQuestionService;

    private final ExamPaperEditPolicy examPaperEditPolicy;
    private final ExamCacheFacade examCacheFacade;

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
        int rows = paperMapper.add(paperManage);
        invalidatePaperRelatedCaches(paperManage.getPaperId());
        return rows;
    }

    @Override
    public int addBatch(List<PaperManage> paperManages) {
        if (paperManages == null || paperManages.isEmpty()) {
            return 0;
        }
        examPaperEditPolicy.assertPaperEditable(paperManages.get(0).getPaperId());
        int rows = paperMapper.batchInsert(paperManages);
        invalidatePaperRelatedCaches(paperManages.get(0).getPaperId());
        return rows;
    }

    // 计算试卷总分：每题 2 分
    @Override
    public Integer getMaxScore(Integer paperId) {
        if (paperId == null) {
            return 0;
        }
        return examCacheFacade.getPaperScore(paperId, () -> {
            Integer questionCount = paperMapper.countByPaperId(paperId);
            return (questionCount == null ? 0 : questionCount) * 2;
        });
    }

    @Override
    public Map<Integer, Integer> getMaxScores(List<Integer> paperIds) {
        if (paperIds == null || paperIds.isEmpty()) {
            return Map.of();
        }
        Map<Integer, Integer> totalScores = new HashMap<>();
        for (Integer paperId : paperIds) {
            if (paperId != null) {
                totalScores.put(paperId, getMaxScore(paperId));
            }
        }
        return totalScores;
    }

    @Override
    public List<Integer> summarizeQuestionTypes(Integer paperId) {
        if (paperId == null) {
            return List.of(0, 0, 0);
        }
        return examCacheFacade.getPaperSummary(paperId, () -> {
            int[] counts = new int[]{0, 0, 0};
            for (Map<String, Object> row : paperMapper.countQuestionsGroupedByType(paperId)) {
                Integer questionType = toInteger(row.get("questionType"));
                Integer questionCount = toInteger(row.get("questionCount"));
                int index = questionType == null ? -1 : questionType - 1;
                if (index >= 0 && index < counts.length) {
                    counts[index] = questionCount == null ? 0 : questionCount;
                }
            }
            return List.of(counts[0], counts[1], counts[2]);
        });
    }

    // 删除试卷中的单条试题关联
    @Override
    public int delete(Integer paperId, Integer type, Integer questionId) {
        examPaperEditPolicy.assertPaperEditable(paperId);
        int rows = paperMapper.delete(paperId, type, questionId);
        invalidatePaperRelatedCaches(paperId);
        return rows;
    }

    // 根据试卷ID删除所有题目关联
    @Override
    public int deleteByPaperId(Integer paperId) {
        examPaperEditPolicy.assertPaperEditable(paperId);
        int rows = paperMapper.deleteByPaperId(paperId);
        invalidatePaperRelatedCaches(paperId);
        return rows;
    }

    private void invalidatePaperRelatedCaches(Integer paperId) {
        if (paperId == null) {
            return;
        }
        examCacheFacade.evictPaperAggregates(paperId);
        examManageMapper.findByPaperId(paperId).forEach(exam -> {
            if (exam.getExamCode() != null) {
                examCacheFacade.evictExamMeta(exam.getExamCode());
                examCacheFacade.evictSnapshotCaches(exam.getExamCode());
            }
        });
        examCacheFacade.bumpScopeExamVersion();
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
