package com.marcowillemart.eventstore;

import com.marcowillemart.eventstore.domain.EventStorageEngine;
import com.marcowillemart.eventstore.domain.subscription.SubscriptionManager;
import com.marcowillemart.eventstore.infrastructure.persistence.JdbcEventStorageEngine;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class ApplicationConfig {

    @Bean
    public EventStorageEngine eventStorageEngine(
            DataSource dataSource,
            PlatformTransactionManager transactionManager) {

        return new JdbcEventStorageEngine(dataSource, transactionManager);
    }

    @Bean
    public SubscriptionManager subscriptionManager(
            EventStorageEngine eventStorageEngine) {

        return new SubscriptionManager(eventStorageEngine);
    }
}
