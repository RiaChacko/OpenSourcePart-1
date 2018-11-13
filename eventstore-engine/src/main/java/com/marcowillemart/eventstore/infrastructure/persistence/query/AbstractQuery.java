package com.marcowillemart.eventstore.infrastructure.persistence.query;

import com.marcowillemart.common.util.Assert;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.transaction.support.TransactionTemplate;

abstract class AbstractQuery implements Query {

    private final String sql;

    protected AbstractQuery(String sql) {
        this.sql = sql;
    }

    @Override
    public <T> T queryForObject(
            TransactionTemplate readOnlyTransactionTemplate,
            JdbcTemplate jdbcTemplate,
            Class<T> requiredType) {

        Assert.isTrue(readOnlyTransactionTemplate.isReadOnly());

        return readOnlyTransactionTemplate.execute(status ->
                jdbcTemplate.queryForObject(sql, requiredType));
    }

    @Override
    public <T> List<T> query(
            TransactionTemplate readOnlyTransactionTemplate,
            JdbcTemplate jdbcTemplate,
            RowMapper<T> rowMapper) {

        Assert.isTrue(readOnlyTransactionTemplate.isReadOnly());

        return readOnlyTransactionTemplate.execute(status ->
                jdbcTemplate.query(
                        connection -> {
                            PreparedStatement preparedStatement =
                                    connection.prepareStatement(sql);

                            build(preparedStatement);

                            return preparedStatement;
                        },
                        (rs, rowNum) -> rowMapper.mapRow(rs)));
    }

    @Override
    public boolean exists(
            TransactionTemplate readOnlyTransactionTemplate,
            JdbcTemplate jdbcTemplate) {

        Assert.isTrue(readOnlyTransactionTemplate.isReadOnly());

        return readOnlyTransactionTemplate.execute(status ->
                jdbcTemplate.query(
                        connection -> {
                            PreparedStatement preparedStatement =
                                    connection.prepareStatement(sql);

                            build(preparedStatement);

                            return preparedStatement;
                        },
                        (ResultSet rs) -> rs.next()));
    }

    @Override
    public Number update(
            TransactionTemplate transactionTemplate,
            JdbcTemplate jdbcTemplate) {

        KeyHolder keyHolder = new GeneratedKeyHolder();

        transactionTemplate.execute(status ->
                jdbcTemplate.update(
                        connection -> {
                            PreparedStatement preparedStatement =
                                    connection.prepareStatement(
                                            sql,
                                            Statement.RETURN_GENERATED_KEYS);

                            build(preparedStatement);

                            return preparedStatement;
                        },
                        keyHolder));

        Optional<Number> key = Optional.of(keyHolder.getKey());
        Assert.isTrue(key.isPresent());
        return key.get();
    }

    @Override
    public void batchUpdate(
            TransactionTemplate transactionTemplate,
            JdbcTemplate jdbcTemplate) {

        transactionTemplate.execute(status ->
                jdbcTemplate.batchUpdate(sql, buildBatch()));
    }

    ////////////////////
    // HELPER METHODS
    ////////////////////

    protected void build(PreparedStatement ps) throws SQLException {
        // no-op
    }

    protected List<Object[]> buildBatch() {
        throw new UnsupportedOperationException("AbstractQuery.buildBatch");
    }
}
