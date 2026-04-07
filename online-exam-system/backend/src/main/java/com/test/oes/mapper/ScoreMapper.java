package com.test.oes.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.oes.entity.Score;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ScoreMapper {

    /**
     * 添加成绩记录
     * @param score 成绩实体
     * @return 影响行数
     */
    @Options(useGeneratedKeys = true, keyProperty = "scoreId")
    @Insert("insert into score(examCode, studentId, subject, ptScore, etScore, score, answerDate) " +
            "values(#{examCode}, #{studentId}, #{subject}, #{ptScore}, #{etScore}, #{score}, #{answerDate})")
    int add(Score score);

    /**
     * 查询所有成绩（不分页）
     * @return 成绩列表
     */
    @Select("select scoreId, examCode, studentId, subject, ptScore, etScore, score, answerDate from score order by scoreId desc")
    List<Score> findAll();

    /**
     * 分页查询某学生的成绩
     * @param page 分页对象
     * @param studentId 学生ID
     * @return 分页结果
     */
    @Select("select scoreId, examCode, studentId, subject, ptScore, etScore, score, answerDate from score " +
            "where studentId = #{studentId} order by scoreId asc")
    IPage<Score> findById(Page<?> page, @Param("studentId") Integer studentId);

    /**
     * 查询某学生的所有成绩（不分页）- 该方法未在参考代码中直接出现，但 Service 接口有需求
     * 注意：参考代码中 Service 层的 findById(Integer studentId) 对应此方法
     */
    @Select("select scoreId, examCode, studentId, subject, ptScore, etScore, score, answerDate from score " +
            "where studentId = #{studentId} order by scoreId asc")
    List<Score> findByStudentId(@Param("studentId") Integer studentId);

    /**
     * 根据考试编号查询成绩列表
     * @param examCode 考试编号
     * @return 成绩列表
     */
    @Select("select * from score where examCode = #{examCode}")
    List<Score> findByExamCode(Integer examCode);
}