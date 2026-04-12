package com.test.oes.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.oes.entity.Score;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

public interface ScoreService {

    /**
     * 添加一条成绩记录
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
    IPage<Score> findById(Page<Score> page, Integer studentId);

    /**
     * 查询某学生的所有成绩（不分页）
     * @param studentId 学生ID
     * @return 成绩列表
     */
    List<Score> findById(Integer studentId);

    /**
     * 根据考试编号查询成绩列表
     * @param examCode 考试编号
     * @return 成绩列表
     */
    List<Score> findByExamCode(Integer examCode);

    // 新增

    /**
     * 获取某考试的成绩统计信息
     * @param examCode 考试编号
     * @return 包含平均分、最高分、最低分、及格率、分数段分布等信息的Map
     */
    Map<String, Object> getStatistics(Integer examCode);
}