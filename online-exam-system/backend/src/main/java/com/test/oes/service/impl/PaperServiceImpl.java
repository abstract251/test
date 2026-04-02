package com.test.oes.service.impl;

import com.test.oes.entity.FillQuestion;
import com.test.oes.entity.JudgeQuestion;
import com.test.oes.entity.MultiQuestion;
import com.test.oes.entity.PaperManage;
import com.test.oes.mapper.PaperMapper;
import com.test.oes.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaperServiceImpl implements PaperService {

    private final PaperMapper paperMapper;

    private final JudgeQuestionService judgeQuestionService;

    private final MultiQuestionService multiQuestionService;

    private final FillQuestionService fillQuestionService;

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
        return paperMapper.add(paperManage);
    }

    // 计算试卷总分：每题 2 分
    @Override
    public Integer getMaxScore(Integer paperId) {

        List<MultiQuestion> multiQuestionRes = multiQuestionService.findByIdAndType(paperId);   //选择题题库 1
        List<FillQuestion> fillQuestionsRes = fillQuestionService.findByIdAndType(paperId);     //填空题题库 2
        List<JudgeQuestion> judgeQuestionRes = judgeQuestionService.findByIdAndType(paperId);   //判断题题库 3
        return 2 * (multiQuestionRes.size() + fillQuestionsRes.size() + judgeQuestionRes.size());
    }

    // 删除试卷中的单条试题关联
    @Override
    public int delete(Integer paperId, Integer type, Integer questionId) {
        return paperMapper.delete(paperId, type, questionId);
    }

    // 根据试卷ID删除所有题目关联
    @Override
    public int deleteByPaperId(Integer paperId) {
        return paperMapper.deleteByPaperId(paperId);
    }
}
