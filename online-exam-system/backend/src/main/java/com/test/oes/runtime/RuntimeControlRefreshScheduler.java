package com.test.oes.runtime;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RuntimeControlRefreshScheduler {

    private final RuntimeControlService runtimeControlService;

    @Scheduled(initialDelay = 1000L, fixedDelay = 3000L)
    public void refresh() {
        try {
            runtimeControlService.refreshSnapshot();
        } catch (Exception ignored) {
        }
    }
}
