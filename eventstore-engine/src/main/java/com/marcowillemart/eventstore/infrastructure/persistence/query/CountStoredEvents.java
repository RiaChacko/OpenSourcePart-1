package com.marcowillemart.eventstore.infrastructure.persistence.query;

import com.marcowillemart.eventstore.infrastructure.persistence.schema.Tables;

public final class CountStoredEvents extends AbstractQuery {

    private static final String SQL =
            String.format("select count(*) from %s", Tables.EVENT_STORE);

    private static final CountStoredEvents INSTANCE = new CountStoredEvents();

    private CountStoredEvents() {
        super(SQL);
    }

    public static CountStoredEvents newQuery() {
        return INSTANCE;
    }
}
