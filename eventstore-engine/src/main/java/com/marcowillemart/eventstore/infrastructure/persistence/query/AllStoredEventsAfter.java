package com.marcowillemart.eventstore.infrastructure.persistence.query;

import com.marcowillemart.eventstore.infrastructure.persistence.schema.Columns;
import com.marcowillemart.eventstore.infrastructure.persistence.schema.Tables;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public final class AllStoredEventsAfter extends AbstractQuery {

    private static final String SQL = String.format(
                "select * from %s where %s > ? order by %s",
                Tables.EVENT_STORE, Columns.EVENT_ID, Columns.EVENT_ID);

    private long eventId;

    private AllStoredEventsAfter() {
        super(SQL);
    }

    public AllStoredEventsAfter eventId(long eventId) {
        this.eventId = eventId;
        return this;
    }

    @Override
    protected void build(PreparedStatement ps) throws SQLException {
        ps.setLong(1, eventId);
    }

    public static AllStoredEventsAfter newQuery() {
        return new AllStoredEventsAfter();
    }
}
