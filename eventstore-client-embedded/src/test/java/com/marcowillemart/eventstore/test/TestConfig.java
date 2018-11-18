package com.marcowillemart.eventstore.test;

import com.marcowillemart.eventstore.domain.EventStorageEngine;
import com.marcowillemart.eventstore.infrastructure.persistence.JdbcEventStorageEngine;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class TestConfig {

    @Bean
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2)
                .addScript("classpath:eventstore/schema.sql")
                .build();
    }

    @Bean
    public PlatformTransactionManager transactionManager(DataSource ds) {
        return new DataSourceTransactionManager(ds);
    }

    @Bean
    public EventStorageEngine eventStorageEngine(
            DataSource dataSource,
            PlatformTransactionManager transactionManager) {

        return new JdbcEventStorageEngine(dataSource, transactionManager);
    }
}
