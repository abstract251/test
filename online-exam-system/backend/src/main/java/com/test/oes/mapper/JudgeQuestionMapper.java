package com.test.oes.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.oes.entity.JudgeQuestion;
import org.apache.ibatis.annotations.*;

import java.util.List;

// 判断题
@Mapper
public interface JudgeQuestionMapper {

    List<JudgeQuestion> findByIdAndType(Integer paperId);

    IPage<JudgeQuestion> findAll(Page<JudgeQuestion> page);

    /**
     * 查询最后一条记录的questionId
     * @return JudgeQuestion
     */
    JudgeQuestion findOnlyQuestionId();

    int add(JudgeQuestion judgeQuestion);

    List<Integer> findIdsBySubject(@Param("subject") String subject);

    int edit(JudgeQuestion judgeQuestion);

    List<JudgeQuestion> findQuestionBySubject(@Param("subject") String subject);

    JudgeQuestion findByQuestionId(@Param("questionId") Integer questionId);

    List<JudgeQuestion> findByQuestionIds(@Param("questionIds") List<Integer> questionIds);

    int deleteByQuestionId(@Param("questionId") Integer questionId);

}
