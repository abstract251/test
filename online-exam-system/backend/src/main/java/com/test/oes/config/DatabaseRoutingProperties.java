package com.test.oes.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.datasource")
public class DatabaseRoutingProperties {

    private boolean readRoutingEnabled = false;
    private Replica replica = new Replica();

    public boolean isReadRoutingEnabled() {
        return readRoutingEnabled;
    }

    public void setReadRoutingEnabled(boolean readRoutingEnabled) {
        this.readRoutingEnabled = readRoutingEnabled;
    }

    public Replica getReplica() {
        return replica;
    }

    public void setReplica(Replica replica) {
        this.replica = replica;
    }

    public static class Replica {
        private boolean enabled = false;
        private String url;
        private String username;
        private String password;
        private String driverClassName = "com.mysql.cj.jdbc.Driver";
        private int maximumPoolSize = 10;
        private int minimumIdle = 1;
        private long connectionTimeout = 2000L;
        private int maxLagSeconds = 5;
        private long healthCheckIntervalMs = 10000L;
        private String validationQuery = "SELECT 1";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getDriverClassName() {
            return driverClassName;
        }

        public void setDriverClassName(String driverClassName) {
            this.driverClassName = driverClassName;
        }

        public int getMaximumPoolSize() {
            return maximumPoolSize;
        }

        public void setMaximumPoolSize(int maximumPoolSize) {
            this.maximumPoolSize = maximumPoolSize;
        }

        public int getMinimumIdle() {
            return minimumIdle;
        }

        public void setMinimumIdle(int minimumIdle) {
            this.minimumIdle = minimumIdle;
        }

        public long getConnectionTimeout() {
            return connectionTimeout;
        }

        public void setConnectionTimeout(long connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
        }

        public int getMaxLagSeconds() {
            return maxLagSeconds;
        }

        public void setMaxLagSeconds(int maxLagSeconds) {
            this.maxLagSeconds = maxLagSeconds;
        }

        public long getHealthCheckIntervalMs() {
            return healthCheckIntervalMs;
        }

        public void setHealthCheckIntervalMs(long healthCheckIntervalMs) {
            this.healthCheckIntervalMs = healthCheckIntervalMs;
        }

        public String getValidationQuery() {
            return validationQuery;
        }

        public void setValidationQuery(String validationQuery) {
            this.validationQuery = validationQuery;
        }
    }
}
