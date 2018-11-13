package com.marcowillemart.eventstore.infrastructure.persistence.query;

import com.marcowillemart.eventstore.infrastructure.persistence.schema.Columns;
import com.marcowillemart.eventstore.infrastructure.persistence.schema.Tables;

public final class LastStoredEventId extends AbstractQuery {

    private static final String SQL = String.format(
            "select max(%s) from %s", Columns.EVENT_ID, Tables.EVENT_STORE);

    private static final LastStoredEventId INSTANCE = new LastStoredEventId();

    private LastStoredEventId() {
        super(SQL);
    }

    public static LastStoredEventId newQuery() {
        return INSTANCE;
    }
}
