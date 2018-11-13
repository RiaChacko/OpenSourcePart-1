package com.marcowillemart.eventstore.infrastructure.persistence.query;

import com.marcowillemart.common.util.Assert;
import com.marcowillemart.eventstore.domain.EventData;
import com.marcowillemart.eventstore.infrastructure.persistence.schema.Columns;
import com.marcowillemart.eventstore.infrastructure.persistence.schema.Tables;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class AppendToStream extends AbstractQuery {

    private static final String SQL = String.format(
                "insert into %s (%s, %s, %s, %s, %s) values (?, ?, ?, ?, ?)",
                Tables.EVENT_STORE,
                Columns.EVENT_TYPE,
                Columns.EVENT_BODY,
                Columns.OCCURRED_ON,
                Columns.STREAM_NAME,
                Columns.STREAM_VERSION);

    private String streamName;
    private int expectedVersion;
    private List<EventData> events;
    private Timestamp occurredOn;

    private AppendToStream() {
        super(SQL);
    }

    public AppendToStream streamName(String streamName) {
        this.streamName = streamName;
        return this;
    }

    public AppendToStream expectedVersion(int expectedVersion) {
        this.expectedVersion = expectedVersion;
        return this;
    }

    @SuppressWarnings("AssignmentToCollectionOrArrayFieldFromParameter")
    public AppendToStream events(List<EventData> events) {
        this.events = events;
        return this;
    }

    public AppendToStream occurredOn(LocalDateTime occurredOn) {
        this.occurredOn = Timestamp.valueOf(occurredOn);
        return this;
    }

    @Override
    protected List<Object[]> buildBatch() {
        List<Object[]> batch = new ArrayList<>(events.size());

        int streamVersion = expectedVersion;

        for (EventData event : events) {
            Assert.notNull(event);

            streamVersion++;

            batch.add(new Object[] {
                event.type(),
                event.body(),
                occurredOn,
                streamName,
                streamVersion
            });
        }

        return batch;
    }

    public static AppendToStream newQuery() {
        return new AppendToStream();
    }
}
