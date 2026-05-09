package com.test.oes.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.oes.entity.ApiResult;
import com.test.oes.entity.Score;
import com.test.oes.security.CurrentUserService;
import com.test.oes.service.ScoreService;
import com.test.oes.util.ApiResultHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ScoreController {

    private final ScoreService scoreService;
    private final CurrentUserService currentUserService;

    /**
     * 查询所有成绩（不分页）
     */
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @GetMapping("/scores")
    public ApiResult<List<Score>> findAll() {
        List<Score> res = scoreService.findAll();
        return ApiResultHandler.buildApiResult(200, "查询所有学生成绩", res);
    }

    /**
     * 分页查询某学生的成绩
     * @param page 页码
     * @param size 每页大小
     * @param studentId 学生ID
     */
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER') or @currentUserService.isCurrentUserId(#studentId)")
    @GetMapping("/score/{page}/{size}/{studentId}")
    public ApiResult<IPage<Score>> findById(@PathVariable Integer page,
                                            @PathVariable Integer size,
                                            @PathVariable Integer studentId) {
        Page<Score> scorePage = new Page<>(page, size);
        IPage<Score> res = scoreService.findById(scorePage, studentId);
        return ApiResultHandler.buildApiResult(200, "根据ID查询成绩", res);
    }

    /**
     * 不分页查询某学生的所有成绩
     * @param studentId 学生ID
     */
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER') or @currentUserService.isCurrentUserId(#studentId)")
    @GetMapping("/score/{studentId}")
    public ApiResult<List<Score>> findById(@PathVariable Integer studentId) {
        List<Score> res = scoreService.findById(studentId);
        if (!res.isEmpty()) {
            return ApiResultHandler.buildApiResult(200, "根据ID查询成绩", res);
        } else {
            return ApiResultHandler.buildApiResult(400, "ID不存在", res);
        }
    }

    /**
     * 添加成绩记录
     */
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @PostMapping("/score")
    public ApiResult<Integer> add(@RequestBody Score score) {
        int res = scoreService.add(score);
        if (res == 0) {
            return ApiResultHandler.buildApiResult(400, "成绩添加失败", res);
        } else {
            return ApiResultHandler.buildApiResult(200, "成绩添加成功", res);
        }
    }

    /**
     * 根据考试编号查询成绩列表
     * @param examCode 考试编号
     */
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @GetMapping("/scores/{examCode}")
    public ApiResult<List<Score>> findByExamCode(@PathVariable Integer examCode) {
        List<Score> scores = scoreService.findByExamCode(examCode);
        return ApiResultHandler.buildApiResult(200, "查询成功", scores);
    }

    // 新增

    /**
     * 获取某考试的成绩统计信息（用于图表展示）
     * @param examCode 考试编号
     * @return 统计信息
     */
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @GetMapping("/score/statistics/{examCode}")
    public ApiResult<Map<String, Object>> getStatistics(@PathVariable Integer examCode) {
        Map<String, Object> statistics = scoreService.getStatistics(examCode);
        return ApiResultHandler.buildApiResult(200, "查询成绩统计成功", statistics);
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @GetMapping("/scores/by-clazz/{examCode}/{clazz}")
    public ApiResult<List<Map<String, Object>>> findByExamCodeAndClazz(@PathVariable Integer examCode,
                                                                       @PathVariable String clazz) {
        List<Map<String, Object>> scores = scoreService.findByExamCodeAndClazz(examCode, clazz);
        return ApiResultHandler.buildApiResult(200, "查询成功", scores);
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @GetMapping("/scores/by-clazz/{examCode}/{page}/{size}/{clazz}")
    public ApiResult<IPage<Map<String, Object>>> findByExamCodeAndClazz(@PathVariable Integer examCode,
                                                                       @PathVariable Integer page,
                                                                       @PathVariable Integer size,
                                                                       @PathVariable String clazz) {
        Page<Map<String, Object>> scorePage = new Page<>(page, size);
        IPage<Map<String, Object>> res = scoreService.findByExamCodeAndClazz(scorePage, examCode, clazz);
        return ApiResultHandler.buildApiResult(200, "查询成功", res);
    }
}
