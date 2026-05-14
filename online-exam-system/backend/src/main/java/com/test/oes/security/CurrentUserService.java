package com.test.oes.security;

import com.test.oes.exception.ExamBusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    public LoginUser requireCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser loginUser)) {
            throw new ExamBusinessException(401, "未登录");
        }
        return loginUser;
    }

    public boolean isCurrentUserId(Integer userId) {
        return requireCurrentUser().getUserId().equals(userId);
    }

    public boolean hasAnyRole(String... roles) {
        String authority = requireCurrentUser().getAccountRole().getAuthority();
        for (String role : roles) {
            if (authority.equals("ROLE_" + role)) {
                return true;
            }
        }
        return false;
    }
}
