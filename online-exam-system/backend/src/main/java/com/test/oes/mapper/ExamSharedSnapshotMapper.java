package com.test.oes.mapper;

import com.test.oes.entity.ExamSharedSnapshot;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ExamSharedSnapshotMapper {

    int insertIgnore(ExamSharedSnapshot row);

    int countByExamCode(@Param("examCode") Integer examCode);

    List<Integer> findExistingExamCodes(@Param("examCodes") List<Integer> examCodes);
}
