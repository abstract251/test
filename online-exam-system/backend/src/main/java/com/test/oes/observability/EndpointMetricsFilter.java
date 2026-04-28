package com.test.oes.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class EndpointMetricsFilter extends OncePerRequestFilter {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private static final List<EndpointRule> ENDPOINT_RULES = List.of(
            new EndpointRule("POST", "/auth/login", "auth.login"),
            new EndpointRule("POST", "/auth/refresh", "auth.refresh"),
            new EndpointRule("GET", "/student/exams", "student.exam.list"),
            new EndpointRule("GET", "/student/exam/*", "student.exam.detail"),
            new EndpointRule("POST", "/student/exam/*/attempt/start", "student.exam.attempt.start"),
            new EndpointRule("PUT", "/student/exam/*/attempt/answers", "student.exam.attempt.answers"),
            new EndpointRule("POST", "/student/exam/*/attempt/submit", "student.exam.attempt.submit"),
            new EndpointRule("POST", "/answer/submit", "answer.submit"),
            new EndpointRule("GET", "/scores", "score.list"),
            new EndpointRule("GET", "/scores/*", "score.exam.list"),
            new EndpointRule("GET", "/score/*/*/*", "score.student.page"),
            new EndpointRule("GET", "/score/*", "score.student.list"),
            new EndpointRule("GET", "/score/statistics/*", "score.statistics"),
            new EndpointRule("GET", "/question-bank/*/*", "question-bank.page")
    );

    private final MeterRegistry meterRegistry;

    public EndpointMetricsFilter(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        EndpointRule rule = matchRule(request);
        if (rule == null) {
            filterChain.doFilter(request, response);
            return;
        }

        Timer.Sample sample = Timer.start(meterRegistry);
        Throwable failure = null;
        try {
            filterChain.doFilter(request, response);
        } catch (IOException | ServletException | RuntimeException ex) {
            failure = ex;
            throw ex;
        } finally {
            int status = resolveStatus(response, failure);
            String outcome = resolveOutcome(status);
            Counter.builder("oes.endpoint.requests")
                    .description("Business endpoint request count")
                    .tag("endpoint", rule.metricTag())
                    .tag("method", request.getMethod())
                    .tag("status", String.valueOf(status))
                    .tag("outcome", outcome)
                    .register(meterRegistry)
                    .increment();
            sample.stop(Timer.builder("oes.endpoint.latency")
                    .description("Business endpoint latency")
                    .tag("endpoint", rule.metricTag())
                    .tag("method", request.getMethod())
                    .tag("status", String.valueOf(status))
                    .tag("outcome", outcome)
                    .register(meterRegistry));
        }
    }

    private EndpointRule matchRule(HttpServletRequest request) {
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }
        String method = request.getMethod();
        for (EndpointRule rule : ENDPOINT_RULES) {
            if (rule.matches(method, path)) {
                return rule;
            }
        }
        return null;
    }

    private int resolveStatus(HttpServletResponse response, Throwable failure) {
        int responseStatus = response.getStatus();
        if (responseStatus > 0) {
            return responseStatus;
        }
        return failure == null ? 200 : 500;
    }

    private String resolveOutcome(int status) {
        if (status >= 500) {
            return "SERVER_ERROR";
        }
        if (status >= 400) {
            return "CLIENT_ERROR";
        }
        return "SUCCESS";
    }

    private record EndpointRule(String method, String pattern, String metricTag) {
        private boolean matches(String requestMethod, String requestPath) {
            return method.equalsIgnoreCase(requestMethod) && PATH_MATCHER.match(pattern, requestPath);
        }
    }
}
