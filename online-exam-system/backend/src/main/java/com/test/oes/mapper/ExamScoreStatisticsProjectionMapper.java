package com.test.oes.mapper;

import com.test.oes.entity.ExamScoreStatisticsProjection;
import org.apache.ibatis.annotations.*;

@Mapper
public interface ExamScoreStatisticsProjectionMapper {

    @Select("""
            SELECT exam_code, avg_score, max_score, min_score, pass_rate, total_count, pass_count,
                   distribution_json, last_event_id, updated_at
            FROM exam_score_statistics_projection
            WHERE exam_code = #{examCode}
            LIMIT 1
            """)
    @Results(id = "projectionMap", value = {
            @Result(column = "exam_code", property = "examCode"),
            @Result(column = "avg_score", property = "avgScore"),
            @Result(column = "max_score", property = "maxScore"),
            @Result(column = "min_score", property = "minScore"),
            @Result(column = "pass_rate", property = "passRate"),
            @Result(column = "total_count", property = "totalCount"),
            @Result(column = "pass_count", property = "passCount"),
            @Result(column = "distribution_json", property = "distributionJson"),
            @Result(column = "last_event_id", property = "lastEventId"),
            @Result(column = "updated_at", property = "updatedAt")
    })
    ExamScoreStatisticsProjection findByExamCode(@Param("examCode") Integer examCode);

    @Insert("""
            INSERT INTO exam_score_statistics_projection(
                exam_code, avg_score, max_score, min_score, pass_rate, total_count, pass_count,
                distribution_json, last_event_id, updated_at
            ) VALUES (
                #{examCode}, #{avgScore}, #{maxScore}, #{minScore}, #{passRate}, #{totalCount}, #{passCount},
                #{distributionJson}, #{lastEventId}, #{updatedAt}
            )
            ON DUPLICATE KEY UPDATE
                avg_score = VALUES(avg_score),
                max_score = VALUES(max_score),
                min_score = VALUES(min_score),
                pass_rate = VALUES(pass_rate),
                total_count = VALUES(total_count),
                pass_count = VALUES(pass_count),
                distribution_json = VALUES(distribution_json),
                last_event_id = VALUES(last_event_id),
                updated_at = VALUES(updated_at)
            """)
    int upsert(ExamScoreStatisticsProjection row);

    @Delete("""
            DELETE FROM exam_score_statistics_projection
            WHERE exam_code = #{examCode}
            """)
    int deleteByExamCode(@Param("examCode") Integer examCode);
}
