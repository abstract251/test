package com.test.oes.async.payload;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuthRefreshRevokedPayload {
    private String jti;
    private Long tokenId;
    private Integer userId;
    private String username;
    private String role;
    private LocalDateTime revokedAt;
    private LocalDateTime expiresAt;
    private String revokeReason;
}
