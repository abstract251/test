package com.test.oes.runtime;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class RuntimeToggleAuditBatchView {
    private String batchId;
    private String presetName;
    private String reason;
    private Integer operatorId;
    private String operatorRole;
    private String operatorName;
    private Integer changeCount;
    private LocalDateTime createdAt;
}
