package com.test.oes.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@ConditionalOnBean(name = "replicaDataSource")
public class ReplicaHealthMonitor {

    private final JdbcTemplate replicaJdbcTemplate;
    private final DatabaseRoutingProperties properties;
    private final DatabaseRouteMetrics metrics;
    private final AtomicBoolean available = new AtomicBoolean(false);
    private final AtomicInteger lagSeconds = new AtomicInteger(-1);

    public ReplicaHealthMonitor(@Qualifier("replicaDataSource") DataSource replicaDataSource,
                                DatabaseRoutingProperties properties,
                                DatabaseRouteMetrics metrics) {
        this.replicaJdbcTemplate = new JdbcTemplate(replicaDataSource);
        this.properties = properties;
        this.metrics = metrics;
    }

    @Scheduled(initialDelay = 1000L, fixedDelayString = "${app.datasource.replica.health-check-interval-ms:10000}")
    public void refresh() {
        try {
            List<Map<String, Object>> rows = replicaJdbcTemplate.queryForList("SHOW REPLICA STATUS");
            if (rows.isEmpty()) {
                replicaJdbcTemplate.queryForObject(properties.getReplica().getValidationQuery(), Integer.class);
                available.set(true);
                lagSeconds.set(0);
                metrics.updateReplicaLag(0);
                return;
            }

            Map<String, Object> row = rows.get(0);
            Integer lag = extractLagSeconds(row);
            int maxLagSeconds = properties.getReplica().getMaxLagSeconds();
            boolean healthy = lag != null && lag >= 0 && lag <= maxLagSeconds;
            available.set(healthy);
            lagSeconds.set(lag == null ? -1 : lag);
            metrics.updateReplicaLag(lag);
        } catch (Exception ex) {
            available.set(false);
            lagSeconds.set(-1);
            metrics.updateReplicaLag(-1);
        }
    }

    public boolean canUseReplica() {
        return available.get();
    }

    public int currentLagSeconds() {
        return lagSeconds.get();
    }

    private Integer extractLagSeconds(Map<String, Object> row) {
        Object raw = row.get("Seconds_Behind_Source");
        if (raw == null) {
            raw = row.get("Seconds_Behind_Master");
        }
        if (raw instanceof Number number) {
            return number.intValue();
        }
        if (raw == null) {
            return null;
        }
        return Integer.parseInt(String.valueOf(raw));
    }
}
