package com.test.oes.runtime;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@AllArgsConstructor
public class RuntimeToggleSnapshot {
    private final long version;
    private final Map<String, Boolean> features;
    private final LocalDateTime refreshedAt;
}
