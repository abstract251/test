package com.test.oes.cache;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
public class StudentDraftCacheValue {
    private Map<String, String> answers = new HashMap<>();
    private Boolean dirty = Boolean.FALSE;
    private LocalDateTime updatedAt;
    private LocalDateTime lastPersistedAt;
}
