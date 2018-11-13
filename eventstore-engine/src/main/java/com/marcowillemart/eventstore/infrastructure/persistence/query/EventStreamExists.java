package com.marcowillemart.eventstore.infrastructure.persistence.query;

import com.marcowillemart.eventstore.infrastructure.persistence.schema.Columns;
import com.marcowillemart.eventstore.infrastructure.persistence.schema.Tables;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public final class EventStreamExists extends AbstractQuery {

    private static final String SQL = String.format(
                "select 1 from %s where %s = ?",
                Tables.EVENT_STORE, Columns.STREAM_NAME);

    private String streamName;

    private EventStreamExists() {
        super(SQL);
    }

    public EventStreamExists streamName(String streamName) {
        this.streamName = streamName;
        return this;
    }

    @Override
    protected void build(PreparedStatement ps) throws SQLException {
        ps.setString(1, streamName);
    }

    public static EventStreamExists newQuery() {
        return new EventStreamExists();
    }
}
