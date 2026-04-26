package com.test.oes.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.oes.entity.FillQuestion;
import org.apache.ibatis.annotations.*;

import java.util.List;

// 填空题
@Mapper
public interface FillQuestionMapper {

    List<FillQuestion> findByIdAndType(Integer paperId);

    IPage<FillQuestion> findAll(Page<FillQuestion> page);

    /**
     * 查询最后一条questionId
     * @return FillQuestion
     */
    FillQuestion findOnlyQuestionId();

    int add(FillQuestion fillQuestion);

    List<Integer> findIdsBySubject(@Param("subject") String subject);

    int edit(FillQuestion fillQuestion);

    List<FillQuestion> findQuestionBySubject(@Param("subject") String subject);

    FillQuestion findByQuestionId(@Param("questionId") Integer questionId);

    List<FillQuestion> findByQuestionIds(@Param("questionIds") List<Integer> questionIds);

    int deleteByQuestionId(@Param("questionId") Integer questionId);

}
