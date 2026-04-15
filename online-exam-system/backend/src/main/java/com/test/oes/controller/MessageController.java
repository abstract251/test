package com.test.oes.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.test.oes.entity.ApiResult;
import com.test.oes.entity.Message;
import com.test.oes.service.impl.MessageServiceImpl;
import com.test.oes.util.ApiResultHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class MessageController {

    private final MessageServiceImpl messageService;

    @GetMapping("/messages/{page}/{size}")
    public ApiResult<IPage<Message>> findAll(@PathVariable("page") Integer page, @PathVariable("size") Integer size) {
        Page<Message> messagePage = new Page<>(page,size);
        IPage<Message> all = messageService.findAll(messagePage);
        return ApiResultHandler.buildApiResult(200,"查询所有留言",all);
    }

    @GetMapping("/message/{id}")
    public ApiResult findById(@PathVariable("id") Integer id) {
        Message res = messageService.findById(id);
        return ApiResultHandler.buildApiResult(200,"根据Id查询",res);
    }

    @DeleteMapping("/message/{id}")
    public ApiResult delete(@PathVariable("id") Integer id) {
        int res = messageService.delete(id);
        if (res != 0) {
            return ApiResultHandler.buildApiResult(200,"删除成功",res);
        } else {
            return ApiResultHandler.buildApiResult(400,"删除失败",res);
        }
    }

    @PostMapping("/message")
    public ApiResult add(@RequestBody Message message) {
        int res = messageService.add(message);
        if (res == 0) {
            return ApiResultHandler.buildApiResult(400,"添加失败",res);
        } else {
            return ApiResultHandler.buildApiResult(200,"添加成功",res);
        }
    }
}