package com.test.oes.mapper;

import com.test.oes.entity.PaperManage;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface PaperMapper {
    List<PaperManage> findAll();

    List<PaperManage> findById(Integer paperId);

    Integer countByPaperId(@Param("paperId") Integer paperId);

    List<Map<String, Object>> countQuestionsByPaperIds(@Param("paperIds") List<Integer> paperIds);

    List<Map<String, Object>> countQuestionsGroupedByType(@Param("paperId") Integer paperId);

    int add(PaperManage paperManage);

    int batchInsert(@Param("rows") List<PaperManage> rows);

    int delete(@Param("paperId") Integer paperId, @Param("type") Integer type, @Param("questionId") Integer questionId);

    List<Integer> findPaperIdsByQuestion(@Param("questionType") Integer questionType, @Param("questionId") Integer questionId);

    int deleteByQuestion(@Param("questionType") Integer questionType, @Param("questionId") Integer questionId);

    /**
     * 根据试卷id删除题目关联
     *
     * @param paperId 试卷id
     */
    int deleteByPaperId(@Param("paperId") Integer paperId);
}
