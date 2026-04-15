package com.test.oes.util;

import com.test.oes.entity.ApiResult;

public class ApiResultHandler {

    public static ApiResult success(Object object) {
        return new ApiResult(200,"请求成功",object);
    }

    public static ApiResult success() {
        return success(null);
    }

    public static <T> ApiResult<T> buildApiResult(Integer code, String message, T data) {
        return new ApiResult<>(code, message, data);
    }
}
