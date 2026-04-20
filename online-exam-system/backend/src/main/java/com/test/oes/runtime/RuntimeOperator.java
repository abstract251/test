package com.test.oes.runtime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RuntimeOperator {
    private final Integer userId;
    private final String role;
    private final String displayName;
}
