package com.test.oes.dto.auth;

import com.test.oes.vo.CurrentUserVO;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthTokenResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;
    private CurrentUserVO user;
}
