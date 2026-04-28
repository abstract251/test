package com.test.oes.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.oes.entity.MultiQuestion;
import com.test.oes.mapper.MultiQuestionMapper;
import com.test.oes.service.MultiQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class MultiQuestionServiceImpl implements MultiQuestionService {

    private final MultiQuestionMapper multiQuestionMapper;

    @Override
    public List<MultiQuestion> findByIdAndType(Integer PaperId) {
        return multiQuestionMapper.findByIdAndType(PaperId);
    }

    @Override
    public IPage<MultiQuestion> findAll(Page<MultiQuestion> page) {
        return multiQuestionMapper.findAll(page);
    }

    @Override
    public MultiQuestion findOnlyQuestionId() {
        return multiQuestionMapper.findOnlyQuestionId();
    }

    @Override
    public int add(MultiQuestion multiQuestion) {
        return multiQuestionMapper.add(multiQuestion);
    }

    @Override
    public List<Integer> findBySubject(String subject, Integer pageNo) {
        List<Integer> candidateIds = new ArrayList<>(multiQuestionMapper.findIdsBySubject(subject));
        if (pageNo == null || pageNo <= 0 || candidateIds.isEmpty()) {
            return List.of();
        }
        Collections.shuffle(candidateIds, ThreadLocalRandom.current());
        return candidateIds.subList(0, Math.min(pageNo, candidateIds.size()));
    }

    @Override
    public int edit(MultiQuestion multiQuestion) {
        return multiQuestionMapper.edit(multiQuestion);
    }

    @Override
    public List<MultiQuestion> findBySubject(String subject) {
        return multiQuestionMapper.findQuestionBySubject(subject);
    }
}
