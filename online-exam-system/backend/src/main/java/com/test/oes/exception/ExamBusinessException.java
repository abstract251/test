package com.test.oes.exception;

import lombok.Getter;

/**
 * 考试业务规则不满足（冻结、撤销、时长等），返回可预期的 HTTP 语义码。
 */
@Getter
public class ExamBusinessException extends RuntimeException {

    private final int httpStyleCode;

    public ExamBusinessException(int httpStyleCode, String message) {
        super(message);
        this.httpStyleCode = httpStyleCode;
    }

}
