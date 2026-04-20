package com.test.oes.runtime;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class RuntimeFeatureToggle {
    private String featureKey;
    private Boolean enabled;
    private Long version;
    private Integer updatedById;
    private String updatedByRole;
    private String updatedByName;
    private String updatedReason;
    private String presetName;
    private LocalDateTime updatedAt;
}
