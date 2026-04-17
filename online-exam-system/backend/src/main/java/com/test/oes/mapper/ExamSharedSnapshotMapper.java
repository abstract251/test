package com.test.oes.mapper;

import com.test.oes.entity.ExamSharedSnapshot;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ExamSharedSnapshotMapper {

    @Insert("INSERT IGNORE INTO exam_shared_snapshot(exam_code, paper_id, created_at) VALUES (#{examCode}, #{paperId}, #{createdAt})")
    int insertIgnore(ExamSharedSnapshot row);

    @Select("SELECT COUNT(*) FROM exam_shared_snapshot WHERE exam_code = #{examCode}")
    int countByExamCode(@Param("examCode") Integer examCode);

    @Select({
            "<script>",
            "SELECT DISTINCT exam_code",
            "FROM exam_shared_snapshot",
            "WHERE exam_code IN",
            "<foreach collection='examCodes' item='examCode' open='(' separator=',' close=')'>",
            "#{examCode}",
            "</foreach>",
            "</script>"
    })
    List<Integer> findExistingExamCodes(@Param("examCodes") List<Integer> examCodes);
}
