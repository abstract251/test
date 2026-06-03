package com.test.oes.vo;

import com.test.oes.security.LoginUser;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CurrentUserVO {
    private Integer userId;
    private String username;
    private String displayName;
    private String role;

    public static CurrentUserVO from(LoginUser loginUser) {
        return new CurrentUserVO(
                loginUser.getUserId(),
                loginUser.getUsername(),
                loginUser.getDisplayName(),
                loginUser.getAccountRole().name()
        );
    }
}
