package com.marcowillemart.eventstore.infrastructure.persistence.query;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

public interface Query {

    <T> T queryForObject(
            TransactionTemplate readOnlyTransactionTemplate,
            JdbcTemplate jdbcTemplate,
            Class<T> requiredType);

    <T> List<T> query(
            TransactionTemplate readOnlyTransactionTemplate,
            JdbcTemplate jdbcTemplate,
            RowMapper<T> rowMapper);

    boolean exists(
            TransactionTemplate readOnlyTransactionTemplate,
            JdbcTemplate jdbcTemplate);

    Number update(
            TransactionTemplate transactionTemplate,
            JdbcTemplate jdbcTemplate);

    void batchUpdate(
            TransactionTemplate transactionTemplate,
            JdbcTemplate jdbcTemplate);
}
