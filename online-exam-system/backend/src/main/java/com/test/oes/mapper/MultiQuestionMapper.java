package com.test.oes.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.oes.entity.MultiQuestion;
import org.apache.ibatis.annotations.*;

import java.util.List;

// 选择题
@Mapper
public interface MultiQuestionMapper {

    List<MultiQuestion> findByIdAndType(Integer PaperId);

    IPage<MultiQuestion> findAll(Page<MultiQuestion> page);

    /**
     * 查询最后一条记录的questionId
     * @return MultiQuestion
     */
    MultiQuestion findOnlyQuestionId();

    int add(MultiQuestion multiQuestion);

    List<Integer> findIdsBySubject(@Param("subject") String subject);

    int edit(MultiQuestion multiQuestion);

    List<MultiQuestion> findQuestionBySubject(@Param("subject") String subject);

    MultiQuestion findByQuestionId(@Param("questionId") Integer questionId);

    List<MultiQuestion> findByQuestionIds(@Param("questionIds") List<Integer> questionIds);

    int deleteByQuestionId(@Param("questionId") Integer questionId);
}
