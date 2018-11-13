package com.marcowillemart.eventstore.infrastructure.persistence;

import com.marcowillemart.common.util.Assert;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.MetaDataAccessException;

final class DatabaseTypes {

    private DatabaseTypes() {
        Assert.shouldNeverGetHere();
    }

    static boolean isPostgres(DataSource dataSource) {
        Assert.notNull(dataSource);

        return "PostgreSQL".equals(databaseProductName(dataSource));
    }

    ////////////////////
    // HELPER METHODS
    ////////////////////

    private static String databaseProductName(DataSource dataSource) {
        try {
            return JdbcUtils.extractDatabaseMetaData(
                    dataSource,
                    "getDatabaseProductName").toString();
        } catch (MetaDataAccessException ex) {
            throw new DataAccessResourceFailureException(
                    "Unable to determine database type: ", ex);
        }
    }
}
