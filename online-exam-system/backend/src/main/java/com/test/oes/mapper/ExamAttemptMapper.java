package com.test.oes.mapper;

import com.test.oes.entity.ExamAttempt;
import org.apache.ibatis.annotations.*;

@Mapper
public interface ExamAttemptMapper {

    @Options(useGeneratedKeys = true, keyProperty = "attemptId")
    @Insert("INSERT INTO exam_attempt(exam_code, student_id, status, answers_json, started_at, submitted_at) "
            + "VALUES (#{examCode}, #{studentId}, #{status}, #{answersJson}, #{startedAt}, #{submittedAt})")
    int insert(ExamAttempt row);

    @Select("SELECT attempt_id, exam_code, student_id, status, answers_json, started_at, submitted_at "
            + "FROM exam_attempt WHERE exam_code = #{examCode} AND student_id = #{studentId}")
    ExamAttempt findByExamAndStudent(@Param("examCode") Integer examCode, @Param("studentId") Integer studentId);

    @Select("SELECT attempt_id, exam_code, student_id, status, answers_json, started_at, submitted_at "
            + "FROM exam_attempt WHERE student_id = #{studentId}")
    java.util.List<ExamAttempt> findByStudentId(@Param("studentId") Integer studentId);

    @Update("UPDATE exam_attempt SET answers_json = #{answersJson} WHERE attempt_id = #{attemptId}")
    int updateAnswers(@Param("attemptId") Long attemptId, @Param("answersJson") String answersJson);

    @Update("UPDATE exam_attempt SET status = #{status}, submitted_at = #{submittedAt} WHERE attempt_id = #{attemptId}")
    int updateStatus(@Param("attemptId") Long attemptId, @Param("status") Integer status,
                     @Param("submittedAt") java.time.LocalDateTime submittedAt);
}
