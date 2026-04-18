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

    @Override
    public Score findByExamAndStudent(Integer examCode, Integer studentId) {
        return scoreMapper.findByExamAndStudent(examCode, studentId);
    }

// 新增

    @Override
    public Map<String, Object> getStatistics(Integer examCode) {
        Map<String, Object> stats = new HashMap<>();

        // 基础统计
        Double avgScore = scoreMapper.getAvgScore(examCode);
        Integer maxScore = scoreMapper.getMaxScore(examCode);
        Integer minScore = scoreMapper.getMinScore(examCode);
        Integer totalCount = scoreMapper.getTotalCount(examCode);
        Integer passCount = scoreMapper.getPassCount(examCode);

        // 及格率（避免除零）
        Double passRate = (totalCount == null || totalCount == 0) ? 0.0
                : (passCount != null ? passCount.doubleValue() / totalCount : 0.0);

        // 分数段分布
        List<Map<String, Object>> distribution = scoreMapper.getScoreDistribution(examCode);

        // 组装返回结果
        stats.put("avgScore", avgScore != null ? avgScore : 0.0);
        stats.put("maxScore", maxScore != null ? maxScore : 0);
        stats.put("minScore", minScore != null ? minScore : 0);
        stats.put("passRate", passRate);
        stats.put("totalCount", totalCount != null ? totalCount : 0);
        stats.put("distribution", distribution);

        return stats;
    }

}
