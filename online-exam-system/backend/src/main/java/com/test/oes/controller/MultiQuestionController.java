package com.test.oes.controller;

import com.test.oes.entity.ApiResult;
import com.test.oes.entity.MultiQuestion;
import com.test.oes.service.impl.MultiQuestionServiceImpl;
import com.test.oes.util.ApiResultHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MultiQuestionController {

    private final MultiQuestionServiceImpl multiQuestionService;

    @GetMapping("/multiQuestionId")
    public ApiResult<MultiQuestion> findOnlyQuestion() {
        MultiQuestion res = multiQuestionService.findOnlyQuestionId();
        return ApiResultHandler.buildApiResult(200,"查询成功",res);
    }

    @PostMapping("/MultiQuestion")
    public ApiResult<Integer> add(@RequestBody MultiQuestion multiQuestion) {
        int res = multiQuestionService.add(multiQuestion);
        if (res != 0) {

            return ApiResultHandler.buildApiResult(200,"添加成功",res);
        }
        return ApiResultHandler.buildApiResult(400,"添加失败",res);
    }

    @PostMapping("/editMultiQuestion")
    public ApiResult<Integer> edit(@RequestBody MultiQuestion multiQuestion) {
        int res = multiQuestionService.edit(multiQuestion);
        if (res != 0) {

            return ApiResultHandler.buildApiResult(200,"修改成功",res);
        }
        return ApiResultHandler.buildApiResult(400,"修改失败",res);
    }
}
