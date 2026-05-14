package com.test.oes.exception;

import com.test.oes.entity.ApiResult;
import com.test.oes.util.ApiResultHandler;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;
import lombok.extern.slf4j.Slf4j;

/**
 * 全局异常处理器（改造版，统一返回JSON，无空白页）
 */
@Slf4j  // 添加日志注解
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ExamBusinessException.class)
    @ResponseBody
    public ApiResult<Void> handleExamBusiness(ExamBusinessException e) {
        log.warn("考试业务规则: {}", e.getMessage());
        return ApiResultHandler.buildApiResult(e.getHttpStyleCode(), e.getMessage(), null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResult<Void> handleAccessDenied(AccessDeniedException e) {
        log.warn("权限不足: {}", e.getMessage());
        return ApiResultHandler.buildApiResult(403, "Forbidden", null);
    }

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

        if (message == null || message.isBlank()) {
            message = "系统异常，请稍后重试";
        } else if (message.contains("(using password: YES)")) {
            if (!message.contains("'root'@'")) {
                message = "数据库账号不存在或没有访问权限，请检查数据库用户名配置";
            } else if (message.contains("'root'@'localhost'")) {
                message = "数据库密码错误或账号无权访问，请检查数据库账号密码配置";
            }
        } else if (message.contains("Table") && message.contains("doesn't exist")) {
            message = "数据库表不存在，请确认已导入初始化 SQL";
        } else if (message.contains("Unknown database")) {
            message = "数据库不存在，请先创建 online_exam 数据库并导入初始化数据";
        } else if (message.contains("edits")) {
            message = "系统更新失败，请稍后重试";
        } else if (message.contains("Failed to obtain JDBC Connection")) {
            message = "数据库连接失败，请确认 MySQL 已启动且 online_exam 数据库可访问";
        } else if (message.contains("SQLSyntaxErrorException")) {
            message = "数据库 SQL 执行失败，请检查表结构与字段是否完整";
        }

        // 返回统一的JSON格式
        return ApiResultHandler.buildApiResult(500, message, null);
    }
}
