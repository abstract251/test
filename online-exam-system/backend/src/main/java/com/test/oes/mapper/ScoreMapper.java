package com.test.oes.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.oes.entity.Score;
import org.apache.ibatis.annotations.*;
import java.util.Map;
import java.util.HashMap;

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

    // 新增的方法

    /**
     * 获取某考试的平均分（etScore）
     */
    @Select("SELECT AVG(etScore) FROM score WHERE examCode = #{examCode}")
    Double getAvgScore(@Param("examCode") Integer examCode);

    /**
     * 获取某考试的最高分
     */
    @Select("SELECT MAX(etScore) FROM score WHERE examCode = #{examCode}")
    Integer getMaxScore(@Param("examCode") Integer examCode);

    /**
     * 获取某考试的最低分
     */
    @Select("SELECT MIN(etScore) FROM score WHERE examCode = #{examCode}")
    Integer getMinScore(@Param("examCode") Integer examCode);

    /**
     * 获取某考试的参考总人数
     */
    @Select("SELECT COUNT(*) FROM score WHERE examCode = #{examCode}")
    Integer getTotalCount(@Param("examCode") Integer examCode);

    /**
     * 获取某考试的及格人数（etScore >= score * 0.6）
     */
    @Select("SELECT COUNT(*) FROM score WHERE examCode = #{examCode} AND etScore >= score * 0.6")
    Integer getPassCount(@Param("examCode") Integer examCode);

    /**
     * 获取某考试的分数段分布
     * 返回格式：每个元素包含 scoreSegment（分数段字符串）和 count（人数）
     */
    @Select("SELECT " +
            "CASE " +
            "  WHEN etScore < 60 THEN '0-59' " +
            "  WHEN etScore >= 60 AND etScore < 70 THEN '60-69' " +
            "  WHEN etScore >= 70 AND etScore < 80 THEN '70-79' " +
            "  WHEN etScore >= 80 AND etScore < 90 THEN '80-89' " +
            "  ELSE '90-100' " +
            "END AS scoreSegment, " +
            "COUNT(*) AS count " +
            "FROM score " +
            "WHERE examCode = #{examCode} " +
            "GROUP BY scoreSegment " +
            "ORDER BY MIN(etScore)")
    List<Map<String, Object>> getScoreDistribution(@Param("examCode") Integer examCode);
}