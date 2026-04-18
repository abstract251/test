package com.test.oes.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.oes.entity.FillQuestion;
import com.test.oes.mapper.FillQuestionMapper;
import com.test.oes.service.FillQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class FillQuestionServiceImpl implements FillQuestionService {

    private final FillQuestionMapper fillQuestionMapper;

    @Override
    public List<FillQuestion> findByIdAndType(Integer paperId) {
        return fillQuestionMapper.findByIdAndType(paperId);
    }

    @Override
    public IPage<FillQuestion> findAll(Page<FillQuestion> page) {
        return fillQuestionMapper.findAll(page);
    }

    @Override
    public FillQuestion findOnlyQuestionId() {
        return fillQuestionMapper.findOnlyQuestionId();
    }

    @Override
    public int add(FillQuestion fillQuestion) {
        return fillQuestionMapper.add(fillQuestion);
    }

    @Override
    public List<Integer> findBySubject(String subject, Integer pageNo) {
        List<Integer> candidateIds = new ArrayList<>(fillQuestionMapper.findIdsBySubject(subject));
        if (pageNo == null || pageNo <= 0 || candidateIds.isEmpty()) {
            return List.of();
        }
        Collections.shuffle(candidateIds, ThreadLocalRandom.current());
        return candidateIds.subList(0, Math.min(pageNo, candidateIds.size()));
    }

    @Override
    public int edit(FillQuestion fillQuestion) {
        return fillQuestionMapper.edit(fillQuestion);
    }

    @Override
    public List<FillQuestion> findBySubject(String subject) {
        return fillQuestionMapper.findQuestionBySubject(subject);
    }
}
