package com.test.oes.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class DbRouteOverrideFilter extends OncePerRequestFilter {

    public static final String ROUTE_OVERRIDE_HEADER = "X-DB-Route";
    private static final String PRIMARY = "primary";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String override = request.getHeader(ROUTE_OVERRIDE_HEADER);
            if (PRIMARY.equalsIgnoreCase(override)) {
                DbRouteContext.forcePrimary();
            }
            filterChain.doFilter(request, response);
        } finally {
            DbRouteContext.clear();
        }
    }
}
