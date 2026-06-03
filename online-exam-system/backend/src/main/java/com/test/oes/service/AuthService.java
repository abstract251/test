package com.test.oes.service;

import com.test.oes.dto.auth.AuthTokenResponse;
import com.test.oes.dto.auth.LoginRequest;
import com.test.oes.vo.CurrentUserVO;

public interface AuthService {
    AuthTokenResponse login(LoginRequest request);

    AuthTokenResponse refresh(String refreshToken);

    void logout(String refreshToken);

    CurrentUserVO currentUser();
}
