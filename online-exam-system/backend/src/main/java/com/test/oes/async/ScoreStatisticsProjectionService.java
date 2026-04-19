package com.test.oes.async;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.oes.cache.ExamCacheFacade;
import com.test.oes.entity.ExamScoreStatisticsProjection;
import com.test.oes.mapper.ExamScoreStatisticsProjectionMapper;
import com.test.oes.mapper.ScoreMapper;
import com.test.oes.vo.ScoreStatisticsSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ScoreStatisticsProjectionService {

    private final ScoreMapper scoreMapper;
    private final ExamScoreStatisticsProjectionMapper projectionMapper;
    private final ExamCacheFacade examCacheFacade;
    private final ObjectMapper objectMapper;

    @Transactional
    public Map<String, Object> rebuildProjection(Integer examCode, String eventId) {
        Map<String, Object> stats = buildRealtimeStatistics(examCode);
        try {
            ExamScoreStatisticsProjection row = new ExamScoreStatisticsProjection();
            row.setExamCode(examCode);
            row.setAvgScore(((Number) stats.get("avgScore")).doubleValue());
            row.setMaxScore(((Number) stats.get("maxScore")).intValue());
            row.setMinScore(((Number) stats.get("minScore")).intValue());
            row.setPassRate(((Number) stats.get("passRate")).doubleValue());
            row.setTotalCount(((Number) stats.get("totalCount")).intValue());
            row.setPassCount(stats.get("passCount") == null ? 0 : ((Number) stats.get("passCount")).intValue());
            row.setDistributionJson(objectMapper.writeValueAsString(stats.get("distribution")));
            row.setLastEventId(eventId);
            row.setUpdatedAt(LocalDateTime.now());
            projectionMapper.upsert(row);
        } catch (Exception exception) {
            return stats;
        }
        examCacheFacade.clearScoreProjectionDirty(examCode);
        examCacheFacade.evictScoreStatistics(examCode);
        examCacheFacade.putScoreStatistics(examCode, stats);
        return stats;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> readProjection(Integer examCode) {
        try {
            ExamScoreStatisticsProjection projection = projectionMapper.findByExamCode(examCode);
            if (projection == null) {
                return null;
            }
            Map<String, Object> stats = new HashMap<>();
            stats.put("avgScore", projection.getAvgScore() == null ? 0.0 : projection.getAvgScore());
            stats.put("maxScore", projection.getMaxScore() == null ? 0 : projection.getMaxScore());
            stats.put("minScore", projection.getMinScore() == null ? 0 : projection.getMinScore());
            stats.put("passRate", projection.getPassRate() == null ? 0.0 : projection.getPassRate());
            stats.put("totalCount", projection.getTotalCount() == null ? 0 : projection.getTotalCount());
            stats.put("passCount", projection.getPassCount() == null ? 0 : projection.getPassCount());
            List<Map<String, Object>> distribution = objectMapper.readValue(
                    projection.getDistributionJson(),
                    new TypeReference<List<Map<String, Object>>>() {
                    }
            );
            stats.put("distribution", distribution);
            return stats;
        } catch (Exception exception) {
            return null;
        }
    }

    @Transactional
    public void deleteProjection(Integer examCode) {
        try {
            projectionMapper.deleteByExamCode(examCode);
        } catch (Exception ignored) {
        }
        examCacheFacade.clearScoreProjectionDirty(examCode);
        examCacheFacade.evictScoreStatistics(examCode);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> buildRealtimeStatistics(Integer examCode) {
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
        stats.put("avgScore", avgScore != null ? avgScore : 0.0);
        stats.put("maxScore", maxScore != null ? maxScore : 0);
        stats.put("minScore", minScore != null ? minScore : 0);
        stats.put("passRate", passRate);
        stats.put("totalCount", totalCount != null ? totalCount : 0);
        stats.put("passCount", passCount != null ? passCount : 0);
        stats.put("distribution", scoreMapper.getScoreDistribution(examCode));
        return stats;
    }
}
