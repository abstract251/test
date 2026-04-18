package com.test.oes.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.oes.entity.Score;
import com.test.oes.mapper.ScoreMapper;
import com.test.oes.service.ScoreService;
import com.test.oes.vo.ScoreStatisticsSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ScoreServiceImpl implements ScoreService {

    private final ScoreMapper scoreMapper;

    @Override
    public int add(Score score) {
        return scoreMapper.add(score);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Score> findAll() {
        return scoreMapper.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public IPage<Score> findById(Page<Score> page, Integer studentId) {
        return scoreMapper.findById(page, studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Score> findById(Integer studentId) {
        return scoreMapper.findByStudentId(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Score> findByExamCode(Integer examCode) {
        return scoreMapper.findByExamCode(examCode);
    }

    @Override
    @Transactional(readOnly = true)
    public Score findByExamAndStudent(Integer examCode, Integer studentId) {
        return scoreMapper.findByExamAndStudent(examCode, studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getStatistics(Integer examCode) {
        Map<String, Object> stats = new HashMap<>();
        ScoreStatisticsSummary summary = scoreMapper.getStatisticsSummary(examCode);
        Double avgScore = summary == null ? null : summary.getAvgScore();
        Integer maxScore = summary == null ? null : summary.getMaxScore();
        Integer minScore = summary == null ? null : summary.getMinScore();
        Integer totalCount = summary == null ? null : summary.getTotalCount();
        Integer passCount = summary == null ? null : summary.getPassCount();

        double passRate = (totalCount == null || totalCount == 0)
                ? 0.0
                : (passCount == null ? 0.0 : passCount.doubleValue() / totalCount.doubleValue());

        List<Map<String, Object>> distribution = scoreMapper.getScoreDistribution(examCode);
        stats.put("avgScore", avgScore != null ? avgScore : 0.0);
        stats.put("maxScore", maxScore != null ? maxScore : 0);
        stats.put("minScore", minScore != null ? minScore : 0);
        stats.put("passRate", passRate);
        stats.put("totalCount", totalCount != null ? totalCount : 0);
        stats.put("distribution", distribution);
        return stats;
    }
}
