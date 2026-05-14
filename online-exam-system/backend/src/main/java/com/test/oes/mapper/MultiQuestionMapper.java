package com.test.oes.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.oes.entity.MultiQuestion;
import org.apache.ibatis.annotations.*;

import java.util.List;

// 选择题
@Mapper
public interface MultiQuestionMapper {

    @Select("select * from multi_question where questionId in (select questionId from paper_manage where questionType = 1 and paperId = #{paperId})")
    List<MultiQuestion> findByIdAndType(Integer PaperId);

    @Select("select * from multi_question")
    IPage<MultiQuestion> findAll(Page<MultiQuestion> page);

    /**
     * 查询最后一条记录的questionId
     * @return MultiQuestion
     */
    @Select("select questionId from multi_question order by questionId desc limit 1")
    MultiQuestion findOnlyQuestionId();

    @Options(useGeneratedKeys = true,keyProperty = "questionId")
    @Insert("insert into multi_question(subject,question,answerA,answerB,answerC,answerD,rightAnswer,analysis,section,level) " +
            "values(#{subject},#{question},#{answerA},#{answerB},#{answerC},#{answerD},#{rightAnswer},#{analysis},#{section},#{level})")
    int add(MultiQuestion multiQuestion);

    @Select("select questionId from multi_question  where subject =#{subject} order by rand() desc limit #{pageNo}")
    List<Integer> findBySubject(@Param("subject") String subject, @Param("pageNo") Integer pageNo);

    @Update("update multi_question set subject = #{subject}, question = #{question}, answerA = #{answerA}, answerB = #{answerB}, answerC = #{answerC}, answerD = #{answerD}, rightAnswer = #{rightAnswer}, analysis = #{analysis}, section = #{section}, level = #{level} where questionId = #{questionId}")
    int edit(MultiQuestion multiQuestion);

    @Select("select * from multi_question where subject =#{subject}")
    List<MultiQuestion> findQuestionBySubject(@Param("subject") String subject);

    @Select("select * from multi_question where questionId = #{questionId}")
    MultiQuestion findByQuestionId(@Param("questionId") Integer questionId);

    @Delete("delete from multi_question where questionId = #{questionId}")
    int deleteByQuestionId(@Param("questionId") Integer questionId);
}
