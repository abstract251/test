package com.test.oes.runtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.oes.util.ApiResultHandler;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RuntimeFeatureGateFilter extends OncePerRequestFilter {

    private final RuntimeToggleManager runtimeToggleManager;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        if (path.startsWith("/question-bank")
                && !runtimeToggleManager.isEnabled(RuntimeFeatureKey.NON_CORE_QUESTION_BANK_ENABLED)) {
            writeDisabled(response, "题库功能已临时降级，请稍后重试");
            return;
        }
        if ((path.startsWith("/messages") || path.startsWith("/message") || path.startsWith("/replay"))
                && !runtimeToggleManager.isEnabled(RuntimeFeatureKey.NON_CORE_MESSAGE_ENABLED)) {
            writeDisabled(response, "留言功能已临时降级，请稍后重试");
            return;
        }
        if (path.startsWith("/practice")
                && !runtimeToggleManager.isEnabled(RuntimeFeatureKey.NON_CORE_PRACTICE_ENABLED)) {
            writeDisabled(response, "练习功能已临时降级，请稍后重试");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private void writeDisabled(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), ApiResultHandler.buildApiResult(503, message, null));
    }
}
