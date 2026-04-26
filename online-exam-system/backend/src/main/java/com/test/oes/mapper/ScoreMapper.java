package com.test.oes.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.oes.entity.Score;
import com.test.oes.vo.ScoreStatisticsSummary;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface ScoreMapper {

    /**
     * 添加成绩记录
     * @param score 成绩实体
     * @return 影响行数
     */
    int add(Score score);

    /**
     * 查询所有成绩（不分页）
     * @return 成绩列表
     */
    List<Score> findAll();

    /**
     * 分页查询某学生的成绩
     * @param page 分页对象
     * @param studentId 学生ID
     * @return 分页结果
     */
    IPage<Score> findById(Page<?> page, @Param("studentId") Integer studentId);

    /**
     * 查询某学生的所有成绩（不分页）- 该方法未在参考代码中直接出现，但 Service 接口有需求
     * 注意：参考代码中 Service 层的 findById(Integer studentId) 对应此方法
     */
    List<Score> findByStudentId(@Param("studentId") Integer studentId);

    /**
     * 根据考试编号查询成绩列表
     * @param examCode 考试编号
     * @return 成绩列表
     */
    List<Score> findByExamCode(Integer examCode);

    Score findByExamAndStudent(@Param("examCode") Integer examCode, @Param("studentId") Integer studentId);

    List<Score> findByExamCodesAndStudent(@Param("examCodes") List<Integer> examCodes,
                                          @Param("studentId") Integer studentId);

    // 新增的方法

    /**
     * 获取某考试的平均分（etScore）
     */
    ScoreStatisticsSummary getStatisticsSummary(@Param("examCode") Integer examCode);

    /**
     * 获取某考试的分数段分布
     * 返回格式：每个元素包含 scoreSegment（分数段字符串）和 count（人数）
     */
    List<Map<String, Object>> getScoreDistribution(@Param("examCode") Integer examCode);
}
