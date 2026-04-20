package com.test.oes.runtime;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class RuntimeFeatureToggleAudit {
    private Long id;
    private String batchId;
    private String featureKey;
    private Boolean oldEnabled;
    private Boolean newEnabled;
    private Integer operatorId;
    private String operatorRole;
    private String operatorName;
    private String reason;
    private String presetName;
    private LocalDateTime createdAt;
}
