package com.test.oes.controller;

import com.test.oes.entity.ApiResult;
import com.test.oes.service.ExamSnapshotService;
import com.test.oes.util.ApiResultHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 学生端：拉取冻结后的本场共用题目快照（与 {@code /paper/{paperId}} 结构一致）。
 */
@RestController
@RequiredArgsConstructor
public class ExamSnapshotController {

    private final ExamSnapshotService examSnapshotService;

    @GetMapping("/exam/{examCode}/frozen-paper")
    public ApiResult<Map<Integer, List<?>>> frozenPaper(@PathVariable Integer examCode) {
        Map<Integer, List<?>> data = examSnapshotService.buildFrozenPaperMap(examCode);
        return ApiResultHandler.buildApiResult(200, "查询成功", data);
    }
}
