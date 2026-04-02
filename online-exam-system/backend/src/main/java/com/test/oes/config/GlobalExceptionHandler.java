package com.test.oes.config;

import com.test.oes.entity.ApiResult;
import com.test.oes.util.ApiResultHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.NoHandlerFoundException;
import lombok.extern.slf4j.Slf4j;

/**
 * 全局异常处理器（改造版，统一返回JSON，无空白页）
 */
@Slf4j  // 添加日志注解
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理【接口不存在 404】
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseBody
    public ApiResult<String> handle404(NoHandlerFoundException e) {
        log.error("404接口不存在：{}", e.getMessage(), e);
        return ApiResultHandler.buildApiResult(404, "接口不存在，请检查URL地址", null);
    }

    /**
     * 处理【空指针异常】（查询不存在的考试/试卷）
     */
    @ExceptionHandler(NullPointerException.class)
    @ResponseBody
    public ApiResult<String> handleNullPointer(NullPointerException e) {
        log.error("空指针异常：{}", e.getMessage(), e);
        return ApiResultHandler.buildApiResult(500, "数据不存在或系统异常", null);
    }

    /**
     * 处理【所有其他异常】
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public ApiResult<String> handleException(Exception e) {
        String message = e.getMessage();
        log.error("系统异常：{}", e.getMessage(), e);

        if (message.contains("(using password: YES)")) {
            if (!message.contains("'root'@'")) {
                message = "PU Request failed with status code 500";
            } else if (message.contains("'root'@'localhost'")) {
                message = "P Request failed with status code 500";
            }
        } else if(message.contains("Table") && message.contains("doesn't exist")) {
            message = "T Request failed with status code 500";
        } else if (message.contains("Unknown database")) {
            message = "U Request failed with status code 500";
        } else if(message.contains("edits")) {
            message = "R Request failed with status code 500";
        } else if(message.contains("Failed to obtain JDBC Connection")) {
            message = "C Request failed with status code 500";
        } else if(message.contains("SQLSyntaxErrorException")) {
            message = "S Request failed with status code 500";
        }

        // 返回统一的JSON格式
        return ApiResultHandler.buildApiResult(500, message, null);
    }
}