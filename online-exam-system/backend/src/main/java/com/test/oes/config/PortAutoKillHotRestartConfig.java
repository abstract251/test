package com.test.oes.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Configuration
public class PortAutoKillHotRestartConfig {

    private static final Logger log = LoggerFactory.getLogger(PortAutoKillHotRestartConfig.class);

    @Bean
    public WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> portAutoKillCustomizer(Environment environment) {
        return factory -> {
            boolean enabled = environment.getProperty("app.hot-start.port-auto-kill.enabled", Boolean.class, false);
            if (!enabled) {
                return;
            }

            String guardToken = environment.getProperty("app.hot-start.port-auto-kill.guard-token", "");
            if (!"LOCAL_SINGLE_INSTANCE_ONLY".equals(guardToken)) {
                log.warn("端口热启动自动清理已忽略：缺少单实例开发保护令牌");
                return;
            }

            int port = environment.getProperty("server.port", Integer.class, 8080);
            if (port <= 0) {
                return;
            }

            log.info("端口热启动自动清理已启用，目标端口={}", port);

            if (!isWindows()) {
                log.warn("端口自动清理仅在 Windows 环境启用，当前系统跳过处理");
                return;
            }

            try {
                freePortIfOccupied(port);
            } catch (Exception ex) {
                throw new IllegalStateException("自动清理端口失败，端口=" + port, ex);
            }
        };
    }

    private void freePortIfOccupied(int port) throws IOException, InterruptedException {
        List<Long> occupiedPids = findListeningPids(port);
        long currentPid = ProcessHandle.current().pid();
        List<Long> targetPids = occupiedPids.stream()
                .filter(pid -> pid != currentPid)
                .distinct()
                .toList();

        if (targetPids.isEmpty()) {
            return;
        }

        for (Long pid : targetPids) {
            log.warn("检测到端口 {} 被进程 {} 占用，准备自动结束该进程", port, pid);
            executePowerShell("Stop-Process -Id " + pid + " -Force -ErrorAction SilentlyContinue");
        }

        waitUntilPortReleased(port, Duration.ofSeconds(5));
    }

    private List<Long> findListeningPids(int port) throws IOException, InterruptedException {
        String command = "(Get-NetTCPConnection -LocalPort " + port + " -State Listen -ErrorAction SilentlyContinue | "
                + "Select-Object -ExpandProperty OwningProcess -Unique)";
        Process process = new ProcessBuilder("powershell.exe", "-NoProfile", "-Command", command)
                .redirectErrorStream(true)
                .start();
        List<Long> pids = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }
                try {
                    pids.add(Long.parseLong(trimmed));
                } catch (NumberFormatException ignored) {
                    // ignore non-pid lines
                }
            }
        }
        process.waitFor();
        return pids;
    }

    private void waitUntilPortReleased(int port, Duration timeout) throws IOException, InterruptedException {
        Instant deadline = Instant.now().plus(timeout);
        while (Instant.now().isBefore(deadline)) {
            if (findListeningPids(port).isEmpty()) {
                return;
            }
            Thread.sleep(200);
        }
        throw new IllegalStateException("端口仍未释放: " + port);
    }

    private void executePowerShell(String command) throws IOException, InterruptedException {
        Process process = new ProcessBuilder("powershell.exe", "-NoProfile", "-Command", command)
                .redirectErrorStream(true)
                .start();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            while (reader.readLine() != null) {
                // consume output to avoid blocking
            }
        }
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IllegalStateException("PowerShell 命令执行失败: " + command + ", exitCode=" + exitCode);
        }
    }

    private boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
    }
}
