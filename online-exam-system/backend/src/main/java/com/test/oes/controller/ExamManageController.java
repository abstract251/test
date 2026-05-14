package com.test.oes.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.oes.entity.ApiResult;
import com.test.oes.entity.ExamManage;
import com.test.oes.service.ExamManageService;
import com.test.oes.service.ExamSnapshotService;
import com.test.oes.service.exam.ExamTimeHelper;
import com.test.oes.util.ApiResultHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
public class ExamManageController {

    private final ExamManageService examManageService;

    private final ExamTimeHelper examTimeHelper;

    private final ExamSnapshotService examSnapshotService;

    // 不分页查询所有试卷
    @GetMapping("/exams")
    public ApiResult<List<ExamManage>> findAll() {
        System.out.println("不分页查询所有试卷");
        return ApiResultHandler.buildApiResult(200, "请求成功！", examManageService.findAll());
    }

    // 分页查询所有试卷
    @GetMapping("/exams/{page}/{size}")
    public ApiResult<IPage<ExamManage>> findAll(@PathVariable Integer page, @PathVariable Integer size) {
        System.out.println("分页查询所有试卷");
        Page<ExamManage> examManage = new Page<>(page, size);
        IPage<ExamManage> all = examManageService.findAll(examManage);
        return ApiResultHandler.buildApiResult(200, "请求成功！", all);
    }

    // 根据ID查找试卷
    @GetMapping("/exam/{examCode}")
    public ApiResult<ExamManage> findById(@PathVariable Integer examCode) {
        System.out.println("根据ID查找试卷");
        ExamManage res = examManageService.findById(examCode);
        if (res == null) {
            return ApiResultHandler.buildApiResult(10000, "考试编号不存在", null);
        }
        return ApiResultHandler.buildApiResult(200, "请求成功！", res);
    }

    /**
     * 考试时间与冻结策略、撤销状态、快照是否就绪（评审稿 §4–§7）。
     */
    @GetMapping("/exam/{examCode}/exam-policy")
    public ApiResult<Map<String, Object>> examPolicy(@PathVariable Integer examCode) {
        ExamManage res = examManageService.findById(examCode);
        if (res == null) {
            return ApiResultHandler.buildApiResult(10000, "考试编号不存在", null);
        }
        var now = examTimeHelper.nowShanghai();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("examCode", examCode);
        body.put("examStartAt", res.getExamStartAt());
        body.put("freezeAt", examTimeHelper.freezeInstant(res));
        body.put("paperLocked", examTimeHelper.isPaperLocked(res, now));
        body.put("revoked", examTimeHelper.isRevoked(res));
        body.put("snapshotReady", examSnapshotService.hasSharedSnapshot(examCode));
        body.put("windowEndAt", examTimeHelper.examWindowEnd(res));
        body.put("inExamWindow", examTimeHelper.isWithinExamWindow(res, now));
        body.put("serverTime", now);
        return ApiResultHandler.buildApiResult(200, "请求成功", body);
    }

    // 根据ID删除试卷
    @DeleteMapping("/exam/{examCode}")
    public ApiResult<Integer> deleteById(@PathVariable Integer examCode) {
        int res = examManageService.delete(examCode);
        return ApiResultHandler.buildApiResult(200, "删除成功", res);
    }

    // 更新试卷
    @PutMapping("/exam")
    public ApiResult<Integer> update(@RequestBody ExamManage exammanage) {
        int res = examManageService.update(exammanage);
        if (res == 0) {
            return ApiResultHandler.buildApiResult(20000, "请求参数错误", res);
        }
        System.out.print("更新操作执行---");
        return ApiResultHandler.buildApiResult(200, "更新成功", res);
    }

    // 添加试卷
    @PostMapping("/exam")
    public ApiResult<Integer> add(@RequestBody ExamManage exammanage) {
        int res = examManageService.add(exammanage);
        if (res == 1) {
            return ApiResultHandler.buildApiResult(200, "添加成功", res);
        } else {
            return ApiResultHandler.buildApiResult(400, "添加失败", res);
        }
    }

    // 查询最后一条记录的试卷
    @GetMapping("/examManagePaperId")
    public ApiResult<ExamManage> findOnlyPaperId() {
        ExamManage res = examManageService.findOnlyPaperId();
        if (res != null) {
            return ApiResultHandler.buildApiResult(200, "请求成功", res);
        }
        return ApiResultHandler.buildApiResult(400, "请求失败", null);
    }
}
