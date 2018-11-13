package com.marcowillemart.eventstore.infrastructure.persistence;

import com.marcowillemart.common.util.Assert;
import com.marcowillemart.eventstore.domain.StoredEventData;
import com.marcowillemart.eventstore.infrastructure.persistence.schema.Columns;
import java.sql.ResultSet;
import java.sql.SQLException;

final class ResultSetTranslator {

    private ResultSetTranslator() {
        Assert.shouldNeverGetHere();
    }

    static StoredEventData storedEventFrom(ResultSet rs) throws SQLException {
        return new StoredEventData(
                Columns.eventId(rs),
                Columns.eventType(rs),
                Columns.eventBody(rs),
                Columns.occurredOn(rs),
                Columns.streamName(rs),
                Columns.streamVersion(rs));
    }

    static boolean existsFrom(ResultSet rs) throws SQLException {
        return rs.first();
    }
}
