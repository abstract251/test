package com.test.oes.controller;

import com.test.oes.entity.*;
import com.test.oes.service.FillQuestionService;
import com.test.oes.service.JudgeQuestionService;
import com.test.oes.service.MultiQuestionService;
import com.test.oes.service.PaperService;
import com.test.oes.util.ApiResultHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class PaperController {

    private final PaperService paperService;

    private final JudgeQuestionService judgeQuestionService;

    private final MultiQuestionService multiQuestionService;

    private final FillQuestionService fillQuestionService;

    // 查询所有试卷关联
    @GetMapping("/papers")
    public ApiResult<List<PaperManage>> findAll() {
        return ApiResultHandler.buildApiResult(200,"请求成功",paperService.findAll());
    }

    // 根据试卷ID查询所有题目（选择、填空、判断）；学生角色禁止调用，防止考前泄题（正式试题仅通过开考接口下发）
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @GetMapping("/paper/{paperId}")
    public ApiResult<Map<Integer, List<?>>> findById(@PathVariable Integer paperId) {
        List<MultiQuestion> multiQuestionRes = multiQuestionService.findByIdAndType(paperId);   //选择题题库 1
        List<FillQuestion> fillQuestionsRes = fillQuestionService.findByIdAndType(paperId);     //填空题题库 2
        List<JudgeQuestion> judgeQuestionRes = judgeQuestionService.findByIdAndType(paperId);   //判断题题库 3
        return getMapApiResult(multiQuestionRes, fillQuestionsRes, judgeQuestionRes);
    }

    // 根据科目查询练习题库
    @GetMapping("/practice/{source}")
    public ApiResult<Map<Integer, List<?>>> findBySubject(@PathVariable("source") String subject) {
        List<MultiQuestion> multiQuestionRes = multiQuestionService.findBySubject(subject);   //选择题题库 1
        List<FillQuestion> fillQuestionsRes = fillQuestionService.findBySubject(subject);     //填空题题库 2
        List<JudgeQuestion> judgeQuestionRes = judgeQuestionService.findBySubject(subject);   //判断题题库 3
        return getMapApiResult(multiQuestionRes, fillQuestionsRes, judgeQuestionRes);
    }

    @NonNull
    private ApiResult<Map<Integer, List<?>>> getMapApiResult(List<MultiQuestion> multiQuestionRes, List<FillQuestion> fillQuestionsRes, List<JudgeQuestion> judgeQuestionRes) {
        Map<Integer, List<?>> map = new HashMap<>();
        map.put(1, multiQuestionRes);
        map.put(2, fillQuestionsRes);
        map.put(3, judgeQuestionRes);
        return ApiResultHandler.buildApiResult(200, "查询成功", map);
    }

    // 添加试卷题目关联
    @PostMapping("/paperManage")
    public ApiResult<Integer> add(@RequestBody PaperManage paperManage) {
        int res = paperService.add(paperManage);
        if (res != 0) {
            return ApiResultHandler.buildApiResult(200, "添加成功", res);
        }
        return ApiResultHandler.buildApiResult(400, "添加失败", res);
    }


    // 删除试卷中的某条试题
    @GetMapping("/paper/delete/{paperId}/{type}/{questionId}")
    public ApiResult<Integer> delete(
            @PathVariable Integer paperId,
            @PathVariable Integer type,
            @PathVariable Integer questionId
    ) {
        int rows = paperService.delete(paperId, type, questionId);
        return ApiResultHandler.buildApiResult(200,"删除成功", rows);
    }

    // 根据试卷ID删除所有题目
    @DeleteMapping("/paper/deleteAll/{paperId}")
    public ApiResult<Integer> deleteByPaperId(@PathVariable Integer paperId) {
        int rows = paperService.deleteByPaperId(paperId);
        return ApiResultHandler.buildApiResult(200, "清空试卷题目成功", rows);
    }

    // 获取试卷总分
    @GetMapping("/paper/score/{paperId}")
    public ApiResult<Integer> getMaxScore(@PathVariable Integer paperId) {
        Integer score = paperService.getMaxScore(paperId);
        return ApiResultHandler.buildApiResult(200, "查询成功", score);
    }
}
