package com.test.oes.config;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.function.Supplier;

public class ReadWriteRoutingDataSource extends AbstractRoutingDataSource {

    public static final String PRIMARY = "primary";
    public static final String REPLICA = "replica";

    private final boolean routingEnabled;
    private final Supplier<Boolean> replicaAvailability;
    private final DatabaseRouteMetrics metrics;

    public ReadWriteRoutingDataSource(boolean routingEnabled,
                                      Supplier<Boolean> replicaAvailability,
                                      DatabaseRouteMetrics metrics) {
        this.routingEnabled = routingEnabled;
        this.replicaAvailability = replicaAvailability;
        this.metrics = metrics;
    }

    @Override
    protected Object determineCurrentLookupKey() {
        if (!routingEnabled || !TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {
            metrics.recordRoute(PRIMARY, routingEnabled ? "selected" : "routing_disabled");
            return PRIMARY;
        }
        if (replicaAvailability.get()) {
            metrics.recordRoute(REPLICA, "selected");
            return REPLICA;
        }
        metrics.recordRoute(PRIMARY, "fallback");
        return PRIMARY;
    }
}
