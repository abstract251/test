package com.test.oes.cache;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RefreshTokenHotState {
    private Long id;
    private Integer userId;
    private String role;
    private String username;
    private Boolean revoked;
    private LocalDateTime expiresAt;
}
