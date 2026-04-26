package com.test.oes.dto.auth;

import com.test.oes.security.AccountRole;
import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
    private AccountRole role;
}
