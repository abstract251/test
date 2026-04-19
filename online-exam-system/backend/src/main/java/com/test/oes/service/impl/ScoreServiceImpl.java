package com.test.oes.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.oes.async.ScoreStatisticsProjectionService;
import com.test.oes.cache.ExamCacheFacade;
import com.test.oes.config.DbRouteContext;
import com.test.oes.entity.Score;
import com.test.oes.mapper.ScoreMapper;
import com.test.oes.service.ScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ScoreServiceImpl implements ScoreService {

    private final ScoreMapper scoreMapper;
    private final ScoreStatisticsProjectionService scoreStatisticsProjectionService;
    private final ExamCacheFacade examCacheFacade;

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
        Map<String, Object> cached = examCacheFacade.getScoreStatistics(examCode);
        if (cached != null && asInt(cached.get("totalCount")) > 0) {
            return cached;
        }
        Map<String, Object> projected = scoreStatisticsProjectionService.readProjection(examCode);
        Map<String, Object> stats;
        if (projected != null && !examCacheFacade.isScoreProjectionDirty(examCode)) {
            stats = projected;
        } else {
            stats = scoreStatisticsProjectionService.buildRealtimeStatisticsFromPrimary(examCode);
        }
        if (asInt(stats.get("totalCount")) == 0) {
            Map<String, Object> primaryStats = scoreStatisticsProjectionService.buildRealtimeStatisticsFromPrimary(examCode);
            if (asInt(primaryStats.get("totalCount")) > 0) {
                stats = primaryStats;
            }
        }
        examCacheFacade.putScoreStatistics(examCode, stats);
        return stats;
    }

    private int asInt(Object value) {
        return value instanceof Number number ? number.intValue() : 0;
    }
}
