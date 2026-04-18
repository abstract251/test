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
            "SELECT COUNT(*) FROM multi_question",
            "<where>",
            "  <if test='subject != null and subject != \"\"'>",
            "    subject = #{subject}",
            "  </if>",
            "  <if test='keyword != null and keyword != \"\"'>",
            "    AND question LIKE CONCAT('%', #{keyword}, '%')",
            "  </if>",
            "</where>",
            "</script>"
    })
    long countMulti(@Param("subject") String subject, @Param("keyword") String keyword);

    @Select({
            "<script>",
            "SELECT COUNT(*) FROM fill_question",
            "<where>",
            "  <if test='subject != null and subject != \"\"'>",
            "    subject = #{subject}",
            "  </if>",
            "  <if test='keyword != null and keyword != \"\"'>",
            "    AND question LIKE CONCAT('%', #{keyword}, '%')",
            "  </if>",
            "</where>",
            "</script>"
    })
    long countFill(@Param("subject") String subject, @Param("keyword") String keyword);

    @Select({
            "<script>",
            "SELECT COUNT(*) FROM judge_question",
            "<where>",
            "  <if test='subject != null and subject != \"\"'>",
            "    subject = #{subject}",
            "  </if>",
            "  <if test='keyword != null and keyword != \"\"'>",
            "    AND question LIKE CONCAT('%', #{keyword}, '%')",
            "  </if>",
            "</where>",
            "</script>"
    })
    long countJudge(@Param("subject") String subject, @Param("keyword") String keyword);

    @Select({
            "<script>",
            "SELECT 1 AS questionType, questionId, subject, question, section, level, score, analysis,",
            "       rightAnswer AS answer, answerA, answerB, answerC, answerD",
            "FROM multi_question",
            "<where>",
            "  <if test='subject != null and subject != \"\"'>",
            "    subject = #{subject}",
            "  </if>",
            "  <if test='keyword != null and keyword != \"\"'>",
            "    AND question LIKE CONCAT('%', #{keyword}, '%')",
            "  </if>",
            "</where>",
            "ORDER BY questionId DESC",
            "LIMIT #{size} OFFSET #{offset}",
            "</script>"
    })
    List<QuestionBankItemVO> selectMultiPage(@Param("offset") long offset,
                                             @Param("size") long size,
                                             @Param("subject") String subject,
                                             @Param("keyword") String keyword);

    @Select({
            "<script>",
            "SELECT 2 AS questionType, questionId, subject, question, section, level, score, analysis,",
            "       answer, NULL AS answerA, NULL AS answerB, NULL AS answerC, NULL AS answerD",
            "FROM fill_question",
            "<where>",
            "  <if test='subject != null and subject != \"\"'>",
            "    subject = #{subject}",
            "  </if>",
            "  <if test='keyword != null and keyword != \"\"'>",
            "    AND question LIKE CONCAT('%', #{keyword}, '%')",
            "  </if>",
            "</where>",
            "ORDER BY questionId DESC",
            "LIMIT #{size} OFFSET #{offset}",
            "</script>"
    })
    List<QuestionBankItemVO> selectFillPage(@Param("offset") long offset,
                                            @Param("size") long size,
                                            @Param("subject") String subject,
                                            @Param("keyword") String keyword);

    @Select({
            "<script>",
            "SELECT 3 AS questionType, questionId, subject, question, section, level, score, analysis,",
            "       answer, NULL AS answerA, NULL AS answerB, NULL AS answerC, NULL AS answerD",
            "FROM judge_question",
            "<where>",
            "  <if test='subject != null and subject != \"\"'>",
            "    subject = #{subject}",
            "  </if>",
            "  <if test='keyword != null and keyword != \"\"'>",
            "    AND question LIKE CONCAT('%', #{keyword}, '%')",
            "  </if>",
            "</where>",
            "ORDER BY questionId DESC",
            "LIMIT #{size} OFFSET #{offset}",
            "</script>"
    })
    List<QuestionBankItemVO> selectJudgePage(@Param("offset") long offset,
                                             @Param("size") long size,
                                             @Param("subject") String subject,
                                             @Param("keyword") String keyword);
}
