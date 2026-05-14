package com.test.oes.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuthRefreshToken {
    private Long id;
    private String jti;
    private Integer userId;
    private String role;
    private String username;
    private LocalDateTime expiresAt;
    private Integer revoked;
    private LocalDateTime createdAt;
}
