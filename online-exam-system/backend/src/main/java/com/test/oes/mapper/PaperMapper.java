package com.test.oes.mapper;

import com.test.oes.entity.PaperManage;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PaperMapper {
    @Select("select paperId, questionType,questionId from paper_manage")
    List<PaperManage> findAll();

    @Select("select paperId, questionType,questionId from paper_manage where paperId = #{paperId}")
    List<PaperManage> findById(Integer paperId);

    @Insert("insert into paper_manage(paperId,questionType,questionId) values " +
            "(#{paperId},#{questionType},#{questionId})")
    int add(PaperManage paperManage);

    @Delete("delete from paper_manage where paperId = #{paperId} and questionType = #{type} and questionId = #{questionId}")
    int delete(@Param("paperId") Integer paperId, @Param("type") Integer type, @Param("questionId") Integer questionId);

    /**
     * 根据试卷id删除题目关联
     *
     * @param paperId 试卷id
     */
    @Delete("DELETE FROM paper_manage WHERE paperId = #{paperId}")
    int deleteByPaperId(@Param("paperId") Integer paperId);
}
