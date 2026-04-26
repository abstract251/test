package com.test.oes.mapper;

import com.test.oes.entity.ExamSharedSnapshotItem;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ExamSharedSnapshotItemMapper {

    int insert(ExamSharedSnapshotItem row);

    List<ExamSharedSnapshotItem> findByExamCode(@Param("examCode") Integer examCode);

    int countByExamCode(@Param("examCode") Integer examCode);

    List<java.util.Map<String, Object>> countGroupedByType(@Param("examCode") Integer examCode);
}
