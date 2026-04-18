package com.test.oes.mapper;

import com.test.oes.entity.ExamSharedSnapshotItem;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ExamSharedSnapshotItemMapper {

    @Insert("INSERT INTO exam_shared_snapshot_item(exam_code, question_type, question_id, display_order) "
            + "VALUES (#{examCode}, #{questionType}, #{questionId}, #{displayOrder})")
    int insert(ExamSharedSnapshotItem row);

    @Select("SELECT id, exam_code, question_type, question_id, display_order FROM exam_shared_snapshot_item "
            + "WHERE exam_code = #{examCode} ORDER BY display_order")
    List<ExamSharedSnapshotItem> findByExamCode(@Param("examCode") Integer examCode);

    @Select("SELECT COUNT(*) FROM exam_shared_snapshot_item WHERE exam_code = #{examCode}")
    int countByExamCode(@Param("examCode") Integer examCode);

    @Select("SELECT question_type as questionType, count(*) as questionCount FROM exam_shared_snapshot_item "
            + "WHERE exam_code = #{examCode} GROUP BY question_type")
    List<java.util.Map<String, Object>> countGroupedByType(@Param("examCode") Integer examCode);
}
