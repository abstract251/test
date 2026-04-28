package com.test.oes.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.oes.entity.JudgeQuestion;
import com.test.oes.mapper.JudgeQuestionMapper;
import com.test.oes.service.JudgeQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class JudgeQuestionServiceImpl implements JudgeQuestionService {

    private final JudgeQuestionMapper judgeQuestionMapper;

    @Override
    public List<JudgeQuestion> findByIdAndType(Integer paperId) {
        return judgeQuestionMapper.findByIdAndType(paperId);
    }

    @Override
    public IPage<JudgeQuestion> findAll(Page<JudgeQuestion> page) {
        return judgeQuestionMapper.findAll(page);
    }

    @Override
    public JudgeQuestion findOnlyQuestionId() {
        return judgeQuestionMapper.findOnlyQuestionId();
    }

    @Override
    public int add(JudgeQuestion judgeQuestion) {
        return judgeQuestionMapper.add(judgeQuestion);
    }

    @Override
    public List<Integer> findBySubject(String subject, Integer pageNo) {
        List<Integer> candidateIds = new ArrayList<>(judgeQuestionMapper.findIdsBySubject(subject));
        if (pageNo == null || pageNo <= 0 || candidateIds.isEmpty()) {
            return List.of();
        }
        Collections.shuffle(candidateIds, ThreadLocalRandom.current());
        return candidateIds.subList(0, Math.min(pageNo, candidateIds.size()));
    }

    @Override
    public int edit(JudgeQuestion judgeQuestion) {
        return judgeQuestionMapper.edit(judgeQuestion);
    }

    @Override
    public List<JudgeQuestion> findBySubject(String subject) {
        return judgeQuestionMapper.findQuestionBySubject(subject);
    }
}
