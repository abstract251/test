package com.test.oes.mapper;

import com.test.oes.entity.ExamAttempt;
import org.apache.ibatis.annotations.*;

@Mapper
public interface ExamAttemptMapper {

    int insert(ExamAttempt row);

    ExamAttempt findByExamAndStudent(@Param("examCode") Integer examCode, @Param("studentId") Integer studentId);

    ExamAttempt findByExamAndStudentForUpdate(@Param("examCode") Integer examCode, @Param("studentId") Integer studentId);

    java.util.List<ExamAttempt> findByStudentId(@Param("studentId") Integer studentId);

    int updateAnswers(@Param("attemptId") Long attemptId, @Param("answersJson") String answersJson);

    int updateStatus(@Param("attemptId") Long attemptId, @Param("status") Integer status,
                     @Param("submittedAt") java.time.LocalDateTime submittedAt);
}
