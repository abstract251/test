package com.test.oes.async;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.oes.cache.ExamCacheFacade;
import com.test.oes.entity.ExamScoreStatisticsProjection;
import com.test.oes.mapper.ExamScoreStatisticsProjectionMapper;
import com.test.oes.mapper.ScoreMapper;
import com.test.oes.vo.ScoreStatisticsSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
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
    @Qualifier("primaryDataSource")
    private final javax.sql.DataSource primaryDataSource;

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

    @Transactional(readOnly = true)
    public Map<String, Object> buildRealtimeStatisticsFromPrimary(Integer examCode) {
        JdbcTemplate primaryJdbcTemplate = new JdbcTemplate(primaryDataSource);
        Map<String, Object> stats = new HashMap<>();
        Map<String, Object> summary = primaryJdbcTemplate.queryForMap("""
                SELECT COALESCE(AVG(etScore), 0) AS avgScore,
                       COALESCE(MAX(etScore), 0) AS maxScore,
                       COALESCE(MIN(etScore), 0) AS minScore,
                       COUNT(*) AS totalCount,
                       COALESCE(SUM(CASE WHEN etScore >= score * 0.6 THEN 1 ELSE 0 END), 0) AS passCount
                FROM score
                WHERE examCode = ?
                """, examCode);
        int totalCount = ((Number) summary.get("totalCount")).intValue();
        int passCount = ((Number) summary.get("passCount")).intValue();
        double passRate = totalCount == 0 ? 0.0 : passCount / (double) totalCount;
        List<Map<String, Object>> distribution = primaryJdbcTemplate.query("""
                SELECT CASE
                           WHEN etScore < 60 THEN '0-59'
                           WHEN etScore >= 60 AND etScore < 70 THEN '60-69'
                           WHEN etScore >= 70 AND etScore < 80 THEN '70-79'
                           WHEN etScore >= 80 AND etScore < 90 THEN '80-89'
                           ELSE '90-100'
                       END AS scoreSegment,
                       COUNT(*) AS count
                FROM score
                WHERE examCode = ?
                GROUP BY scoreSegment
                ORDER BY MIN(etScore)
                """, (rs, rowNum) -> {
            Map<String, Object> item = new HashMap<>();
            item.put("scoreSegment", rs.getString("scoreSegment"));
            item.put("count", rs.getInt("count"));
            return item;
        }, examCode);
        stats.put("avgScore", ((Number) summary.get("avgScore")).doubleValue());
        stats.put("maxScore", ((Number) summary.get("maxScore")).intValue());
        stats.put("minScore", ((Number) summary.get("minScore")).intValue());
        stats.put("passRate", passRate);
        stats.put("totalCount", totalCount);
        stats.put("passCount", passCount);
        stats.put("distribution", distribution);
        return stats;
    }
}
