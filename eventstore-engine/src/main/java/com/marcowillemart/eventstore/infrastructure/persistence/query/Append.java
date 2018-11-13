package com.marcowillemart.eventstore.infrastructure.persistence.query;

import com.marcowillemart.eventstore.infrastructure.persistence.schema.Columns;
import com.marcowillemart.eventstore.infrastructure.persistence.schema.Tables;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public final class Append extends AbstractQuery {

    private static final String SQL_STANDARD = String.format(
                "insert into %s (%s, %s, %s) values (?, ?, ?)",
                Tables.EVENT_STORE,
                Columns.EVENT_TYPE,
                Columns.EVENT_BODY,
                Columns.OCCURRED_ON);

    private static final String SQL_POSTGRES = String.format(
                "insert into %s (%s, %s, %s) values (?, ?, ?) returning %s",
                Tables.EVENT_STORE,
                Columns.EVENT_TYPE,
                Columns.EVENT_BODY,
                Columns.OCCURRED_ON,
                Columns.EVENT_ID);

    private String eventType;
    private byte[] eventBody;
    private Timestamp occurredOn;

    private Append(boolean postgres) {
        super(postgres ? SQL_POSTGRES : SQL_STANDARD);
    }

    public Append eventType(String eventType) {
        this.eventType = eventType;
        return this;
    }

    @SuppressWarnings("AssignmentToCollectionOrArrayFieldFromParameter")
    public Append eventBody(byte[] eventBody) {
        this.eventBody = eventBody;
        return this;
    }

    public Append occurredOn(LocalDateTime occurredOn) {
        this.occurredOn = Timestamp.valueOf(occurredOn);
        return this;
    }

    @Override
    protected void build(PreparedStatement ps) throws SQLException {
        ps.setString(1, eventType);
        ps.setBytes(2, eventBody);
        ps.setTimestamp(3, occurredOn);
    }

    public static Append newQuery(boolean postgres) {
        return new Append(postgres);
    }
}
