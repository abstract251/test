package com.test.oes.mapper;

import com.test.oes.vo.QuestionBankItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface QuestionBankMapper {

    @Select({
            "<script>",
            "SELECT COUNT(*) FROM (",
            "  SELECT 1 AS questionType, questionId, subject, question, section, level, score, analysis,",
            "         rightAnswer AS answer, answerA, answerB, answerC, answerD",
            "  FROM multi_question",
            "  UNION ALL",
            "  SELECT 2 AS questionType, questionId, subject, question, section, level, score, analysis,",
            "         answer, NULL AS answerA, NULL AS answerB, NULL AS answerC, NULL AS answerD",
            "  FROM fill_question",
            "  UNION ALL",
            "  SELECT 3 AS questionType, questionId, subject, question, section, level, score, analysis,",
            "         answer, NULL AS answerA, NULL AS answerB, NULL AS answerC, NULL AS answerD",
            "  FROM judge_question",
            ") qb",
            "<where>",
            "  <if test='questionType != null'>",
            "    qb.questionType = #{questionType}",
            "  </if>",
            "  <if test='subject != null and subject != \"\"'>",
            "    AND qb.subject = #{subject}",
            "  </if>",
            "  <if test='keyword != null and keyword != \"\"'>",
            "    AND qb.question LIKE CONCAT('%', #{keyword}, '%')",
            "  </if>",
            "</where>",
            "</script>"
    })
    long countAll(@Param("questionType") Integer questionType,
                  @Param("subject") String subject,
                  @Param("keyword") String keyword);

    @Select({
            "<script>",
            "SELECT qb.questionType, qb.questionId, qb.subject, qb.question, qb.section, qb.level,",
            "       qb.score, qb.analysis, qb.answer, qb.answerA, qb.answerB, qb.answerC, qb.answerD",
            "FROM (",
            "  SELECT 1 AS questionType, questionId, subject, question, section, level, score, analysis,",
            "         rightAnswer AS answer, answerA, answerB, answerC, answerD",
            "  FROM multi_question",
            "  UNION ALL",
            "  SELECT 2 AS questionType, questionId, subject, question, section, level, score, analysis,",
            "         answer, NULL AS answerA, NULL AS answerB, NULL AS answerC, NULL AS answerD",
            "  FROM fill_question",
            "  UNION ALL",
            "  SELECT 3 AS questionType, questionId, subject, question, section, level, score, analysis,",
            "         answer, NULL AS answerA, NULL AS answerB, NULL AS answerC, NULL AS answerD",
            "  FROM judge_question",
            ") qb",
            "<where>",
            "  <if test='questionType != null'>",
            "    qb.questionType = #{questionType}",
            "  </if>",
            "  <if test='subject != null and subject != \"\"'>",
            "    AND qb.subject = #{subject}",
            "  </if>",
            "  <if test='keyword != null and keyword != \"\"'>",
            "    AND qb.question LIKE CONCAT('%', #{keyword}, '%')",
            "  </if>",
            "</where>",
            "ORDER BY qb.questionId DESC, qb.questionType ASC",
            "LIMIT #{size} OFFSET #{offset}",
            "</script>"
    })
    List<QuestionBankItemVO> selectPage(@Param("offset") long offset,
                                        @Param("size") long size,
                                        @Param("questionType") Integer questionType,
                                        @Param("subject") String subject,
                                        @Param("keyword") String keyword);
}
