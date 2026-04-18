package com.test.oes.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableScheduling
@EnableConfigurationProperties(DatabaseRoutingProperties.class)
public class DataSourceRoutingConfig {

    @Bean
    @Primary
    @org.springframework.boot.context.properties.ConfigurationProperties("spring.datasource")
    public DataSourceProperties primaryDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "primaryDataSource")
    @org.springframework.boot.context.properties.ConfigurationProperties("spring.datasource.hikari")
    public HikariDataSource primaryDataSource(@Qualifier("primaryDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean(name = "replicaDataSource")
    @ConditionalOnProperty(prefix = "app.datasource.replica", name = "enabled", havingValue = "true")
    public HikariDataSource replicaDataSource(DatabaseRoutingProperties properties) {
        DatabaseRoutingProperties.Replica replica = properties.getReplica();
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(replica.getUrl());
        dataSource.setUsername(replica.getUsername());
        dataSource.setPassword(replica.getPassword());
        dataSource.setDriverClassName(replica.getDriverClassName());
        dataSource.setMaximumPoolSize(replica.getMaximumPoolSize());
        dataSource.setMinimumIdle(replica.getMinimumIdle());
        dataSource.setConnectionTimeout(replica.getConnectionTimeout());
        dataSource.setPoolName("online-exam-replica");
        return dataSource;
    }

    @Bean(name = "dataSource")
    @Primary
    public DataSource dataSource(@Qualifier("primaryDataSource") DataSource primaryDataSource,
                                 @Qualifier("replicaDataSource") ObjectProvider<DataSource> replicaDataSourceProvider,
                                 ObjectProvider<ReplicaHealthMonitor> replicaHealthMonitorProvider,
                                 DatabaseRoutingProperties properties,
                                 DatabaseRouteMetrics metrics) {
        DataSource replicaDataSource = replicaDataSourceProvider.getIfAvailable();
        if (!properties.isReadRoutingEnabled() || replicaDataSource == null) {
            return new LazyConnectionDataSourceProxy(primaryDataSource);
        }

        ReadWriteRoutingDataSource routingDataSource = new ReadWriteRoutingDataSource(
                true,
                () -> {
                    ReplicaHealthMonitor monitor = replicaHealthMonitorProvider.getIfAvailable();
                    return monitor != null && monitor.canUseReplica();
                },
                metrics
        );
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put(ReadWriteRoutingDataSource.PRIMARY, primaryDataSource);
        targetDataSources.put(ReadWriteRoutingDataSource.REPLICA, replicaDataSource);
        routingDataSource.setTargetDataSources(targetDataSources);
        routingDataSource.setDefaultTargetDataSource(primaryDataSource);
        routingDataSource.afterPropertiesSet();
        return new LazyConnectionDataSourceProxy(routingDataSource);
    }
}
