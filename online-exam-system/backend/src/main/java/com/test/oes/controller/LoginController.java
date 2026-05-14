package com.test.oes.controller;

import com.test.oes.dto.auth.AuthTokenResponse;
import com.test.oes.dto.auth.LoginRequest;
import com.test.oes.dto.auth.LogoutRequest;
import com.test.oes.dto.auth.RefreshTokenRequest;
import com.test.oes.entity.ApiResult;
import com.test.oes.service.AuthService;
import com.test.oes.vo.CurrentUserVO;
import com.test.oes.util.ApiResultHandler;
import org.springframework.web.bind.annotation.*;


@RestController
public class LoginController {

    private final AuthService authService;

    public LoginController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/auth/login")
    public ApiResult<AuthTokenResponse> login(@RequestBody LoginRequest loginRequest) {
        return ApiResultHandler.buildApiResult(200, "请求成功", authService.login(loginRequest));
    }

    @PostMapping("/auth/refresh")
    public ApiResult<AuthTokenResponse> refresh(@RequestBody RefreshTokenRequest request) {
        return ApiResultHandler.buildApiResult(200, "请求成功", authService.refresh(request.getRefreshToken()));
    }

    @PostMapping("/auth/logout")
    public ApiResult<Void> logout(@RequestBody(required = false) LogoutRequest request) {
        authService.logout(request == null ? null : request.getRefreshToken());
        return ApiResultHandler.buildApiResult(200, "退出成功", null);
    }

    @GetMapping("/auth/me")
    public ApiResult<CurrentUserVO> currentUser() {
        return ApiResultHandler.buildApiResult(200, "请求成功", authService.currentUser());
    }
}
