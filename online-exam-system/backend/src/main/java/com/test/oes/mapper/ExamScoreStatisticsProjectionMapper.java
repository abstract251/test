package com.test.oes.mapper;

import com.test.oes.entity.ExamScoreStatisticsProjection;
import org.apache.ibatis.annotations.*;

@Mapper
public interface ExamScoreStatisticsProjectionMapper {

    ExamScoreStatisticsProjection findByExamCode(@Param("examCode") Integer examCode);

    int upsert(ExamScoreStatisticsProjection row);

    int deleteByExamCode(@Param("examCode") Integer examCode);
}
