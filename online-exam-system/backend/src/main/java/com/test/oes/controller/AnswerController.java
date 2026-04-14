package com.test.oes.controller;

import com.test.oes.entity.ApiResult;
import com.test.oes.entity.Score;
import com.test.oes.service.AnswerService;
import com.test.oes.util.ApiResultHandler;
import com.test.oes.vo.AnswerVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AnswerController {

    private final AnswerService answerService;

    @PostMapping("/answer/submit")
    public ApiResult<Score> submitAnswer(@RequestBody AnswerVO vo) {
        try {
            Score score = answerService.submitAnswer(vo);
            return ApiResultHandler.buildApiResult(200, "交卷成功，成绩已记录", score);
        } catch (RuntimeException e) {
            return ApiResultHandler.buildApiResult(400, e.getMessage(), null);
        }
    }
}
