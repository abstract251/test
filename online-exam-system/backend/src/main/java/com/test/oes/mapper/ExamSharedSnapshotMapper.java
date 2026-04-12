package com.test.oes.mapper;

import com.test.oes.entity.ExamSharedSnapshot;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ExamSharedSnapshotMapper {

    @Insert("INSERT INTO exam_shared_snapshot(exam_code, paper_id, created_at) VALUES (#{examCode}, #{paperId}, #{createdAt})")
    int insert(ExamSharedSnapshot row);

    @Select("SELECT COUNT(*) FROM exam_shared_snapshot WHERE exam_code = #{examCode}")
    int countByExamCode(@Param("examCode") Integer examCode);
}
