package com.test.oes.controller;

import com.test.oes.entity.ApiResult;
import com.test.oes.entity.Replay;
import com.test.oes.service.impl.ReplayServiceImpl;
import com.test.oes.util.ApiResultHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ReplayController {

    private final ReplayServiceImpl replayService;

    @PostMapping("/replay")
    public ApiResult add(@RequestBody Replay replay) {
        int data = replayService.add(replay);
        if (data != 0) {
            return ApiResultHandler.buildApiResult(200,"添加成功！",data);
        } else {
            return ApiResultHandler.buildApiResult(400,"添加失败！",null);
        }
    }

    @GetMapping("/replay/{messageId}")
    public ApiResult findAllById(@PathVariable("messageId") Integer messageId) {
        List<Replay> res = replayService.findAllById(messageId);
        return ApiResultHandler.buildApiResult(200,"根据messageId查询",res);
    }
}