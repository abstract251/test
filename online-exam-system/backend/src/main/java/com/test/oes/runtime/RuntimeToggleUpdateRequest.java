package com.test.oes.runtime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RuntimeToggleUpdateRequest {
    private Boolean enabled;
    private String reason;
}
