package com.marcowillemart.eventstore.infrastructure.persistence.query;

import com.marcowillemart.eventstore.infrastructure.persistence.schema.Columns;
import com.marcowillemart.eventstore.infrastructure.persistence.schema.Tables;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public final class AllStoredEventsBetween extends AbstractQuery {

    private static final String SQL = String.format(
            "select * from %s where %s between ? and ? order by %s asc",
            Tables.EVENT_STORE, Columns.EVENT_ID, Columns.EVENT_ID);

    private long lowEventId;
    private long highEventId;

    private AllStoredEventsBetween() {
        super(SQL);
    }

    public AllStoredEventsBetween lowEventId(long lowEventId) {
        this.lowEventId = lowEventId;
        return this;
    }

    public AllStoredEventsBetween highEventId(long highEventId) {
        this.highEventId = highEventId;
        return this;
    }

    @Override
    protected void build(PreparedStatement ps) throws SQLException {
        ps.setLong(1, lowEventId);
        ps.setLong(2, highEventId);
    }

    public static AllStoredEventsBetween newQuery() {
        return new AllStoredEventsBetween();
    }
}
