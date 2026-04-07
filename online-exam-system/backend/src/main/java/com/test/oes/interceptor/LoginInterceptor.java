package com.test.oes.interceptor;

import com.test.oes.util.ApiResultHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Set;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    private static final Set<String> VALID_ROLES = Set.of("0", "1", "2");
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = null;
        String role = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("rb_token".equals(cookie.getName())) {
                    token = cookie.getValue();
                } else if ("rb_role".equals(cookie.getName())) {
                    role = cookie.getValue();
                }
            }
        }

        if (token != null && !token.isBlank() && role != null && VALID_ROLES.contains(role)) {
            return true;
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(
                ApiResultHandler.buildApiResult(401, "Unauthorized or login expired", null)
        ));
        return false;
    }
}