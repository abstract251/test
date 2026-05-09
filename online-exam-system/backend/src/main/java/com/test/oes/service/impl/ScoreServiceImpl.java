package com.test.oes.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.oes.entity.Score;
import com.test.oes.mapper.ScoreMapper;
import com.test.oes.service.ScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.HashMap;

import java.util.List;

@Service
public class ScoreServiceImpl implements ScoreService {

    @Autowired
    private ScoreMapper scoreMapper;

    @Override
    public int add(Score score) {
        return scoreMapper.add(score);
    }

    @Override
    public List<Score> findAll() {
        return scoreMapper.findAll();
    }

    @Override
    public IPage<Score> findById(Page<Score> page, Integer studentId) {
        // 调用 Mapper 中定义的分页查询方法
        return scoreMapper.findById(page, studentId);
    }

    @Override
    public List<Score> findById(Integer studentId) {
        // 调用 Mapper 中定义的不分页查询方法
        return scoreMapper.findByStudentId(studentId);
    }

    @Override
    public List<Score> findByExamCode(Integer examCode) {
        return scoreMapper.findByExamCode(examCode);
    }
// 新增

    @Override
    public Map<String, Object> getStatistics(Integer examCode) {
        Map<String, Object> stats = new HashMap<>();

        Double avgScore = scoreMapper.getAvgScore(examCode);
        Integer maxScore = scoreMapper.getMaxScore(examCode);
        Integer minScore = scoreMapper.getMinScore(examCode);
        Integer totalCount = scoreMapper.getTotalCount(examCode);
        Integer passCount = scoreMapper.getPassCount(examCode);

        Double passRate = (totalCount == null || totalCount == 0) ? 0.0
                : (passCount != null ? passCount.doubleValue() / totalCount : 0.0);

        List<Map<String, Object>> distribution = scoreMapper.getScoreDistribution(examCode);

        stats.put("avgScore", avgScore != null ? avgScore : 0.0);
        stats.put("maxScore", maxScore != null ? maxScore : 0);
        stats.put("minScore", minScore != null ? minScore : 0);
        stats.put("passRate", passRate);
        stats.put("totalCount", totalCount != null ? totalCount : 0);
        stats.put("distribution", distribution);

        return stats;
    }

    @Override
    public List<Map<String, Object>> findByExamCodeAndClazz(Integer examCode, String clazz) {
        return scoreMapper.findByExamCodeAndClazz(examCode, clazz);
    }

    @Override
    public IPage<Map<String, Object>> findByExamCodeAndClazz(Page<?> page, Integer examCode, String clazz) {
        return scoreMapper.findByExamCodeAndClazz(page, examCode, clazz);
    }
}