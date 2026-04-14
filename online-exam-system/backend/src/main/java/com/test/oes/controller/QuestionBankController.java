package com.test.oes.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.oes.entity.ApiResult;
import com.test.oes.service.QuestionBankService;
import com.test.oes.util.ApiResultHandler;
import com.test.oes.vo.QuestionBankItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class QuestionBankController {

    private final QuestionBankService questionBankService;

    @GetMapping("/question-bank/{page}/{size}")
    public ApiResult<Page<QuestionBankItemVO>> findAll(@PathVariable Integer page,
                                                       @PathVariable Integer size,
                                                       @RequestParam(required = false) Integer questionType,
                                                       @RequestParam(required = false) String subject,
                                                       @RequestParam(required = false) String keyword) {
        Page<QuestionBankItemVO> result = questionBankService.findAll(page, size, questionType, subject, keyword);
        return ApiResultHandler.buildApiResult(200, "查询成功", result);
    }

    @DeleteMapping("/question-bank/{questionType}/{questionId}")
    public ApiResult<Map<String, Object>> delete(@PathVariable Integer questionType,
                                                 @PathVariable Integer questionId) {
        Map<String, Object> result = questionBankService.deleteQuestion(questionType, questionId);
        return ApiResultHandler.buildApiResult(200, "删除成功", result);
    }
}
