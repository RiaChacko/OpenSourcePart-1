package com.marcowillemart.eventstore.embedded;

import com.marcowillemart.common.event.EventStore;
import com.marcowillemart.common.util.Assert;
import com.marcowillemart.eventstore.infrastructure.JdbcEventStore;
import com.marcowillemart.eventstore.infrastructure.persistence.JdbcEventStorageEngine;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableConfigurationProperties(EmbeddedEventStoreProperties.class)
@ConditionalOnProperty(
        prefix = "eventstore.embedded",
        name = "enabled",
        havingValue = "true")
public class EmbeddedEventStoreAutoConfiguration {

    private final EmbeddedEventStoreProperties properties;

    public EmbeddedEventStoreAutoConfiguration(
            EmbeddedEventStoreProperties properties) {

        Assert.notNull(properties);

        this.properties = properties;
    }

    @Bean
    public EventStore jdbcEventStore() {
        DataSource dataSource = dataSourceFrom(properties);

        migrate(dataSource);

        return new JdbcEventStore(
                new JdbcEventStorageEngine(
                        dataSource,
                        transactionManagerFrom(dataSource)));
    }

    ////////////////////
    // HELPER METHODS
    ////////////////////

    private static DataSource dataSourceFrom(
            EmbeddedEventStoreProperties properties) {

        return properties.initializeDataSourceBuilder().build();
    }

    private static PlatformTransactionManager transactionManagerFrom(
            DataSource dataSource) {

        return new DataSourceTransactionManager(dataSource);
    }

    private static void migrate(DataSource dataSource) {
        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource);
        // TODO: Externalize as a configuration property
        flyway.setLocations("eventstore/migration");
        flyway.migrate();
    }
}
