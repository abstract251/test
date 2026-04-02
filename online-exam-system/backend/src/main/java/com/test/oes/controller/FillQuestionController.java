package com.test.oes.controller;

import com.test.oes.entity.ApiResult;
import com.test.oes.entity.FillQuestion;
import com.test.oes.service.impl.FillQuestionServiceImpl;
import com.test.oes.util.ApiResultHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class FillQuestionController {

    private final FillQuestionServiceImpl fillQuestionService;

    @PostMapping("/fillQuestion")
    public ApiResult<Integer> add(@RequestBody FillQuestion fillQuestion) {
        int res = fillQuestionService.add(fillQuestion);
        if (res != 0) {
            return ApiResultHandler.buildApiResult(200,"添加成功",res);
        }
        return ApiResultHandler.buildApiResult(400,"添加失败",res);
    }

    @GetMapping("/fillQuestionId")
    public ApiResult<FillQuestion> findOnlyQuestionId() {
        FillQuestion res = fillQuestionService.findOnlyQuestionId();
        return ApiResultHandler.buildApiResult(200,"查询成功",res);
    }

    @PostMapping("/editFillQuestion")
    public ApiResult<Integer> edit(@RequestBody FillQuestion fillQuestion) {
        int res = fillQuestionService.edit(fillQuestion);
        if (res != 0) {
            return ApiResultHandler.buildApiResult(200,"修改成功",res);
        }
        return ApiResultHandler.buildApiResult(400,"修改失败",res);
    }
}
