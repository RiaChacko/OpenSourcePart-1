package com.marcowillemart.eventstore.test;

import com.google.protobuf.Message;
import com.marcowillemart.common.event.EventStreamId;
import com.marcowillemart.common.event.Snapshot;
import com.marcowillemart.common.event.StoredEvent;
import static com.marcowillemart.common.test.Tests.*;
import com.marcowillemart.common.util.Assert;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

public final class Tests {

    private static final AtomicInteger LAST_ID = new AtomicInteger();

    private Tests() {
        Assert.shouldNeverGetHere();
    }

    ////////////////////
    // CREATION METHODS
    ////////////////////

    public static EventStreamId createAnonymousEventStreamId() {
        return new EventStreamId(
                createAnonymousName(),
                createPositiveRandomNumber());
    }

    public static Snapshot createAnonymousSnapshot(String identity) {
        return new Snapshot(
                identity,
                createPositiveRandomNumber(),
                createAnonymousMessage());
    }

    public static StoredEvent createAnonymousStoredEvent() {
        Message event = createAnonymousDtoSample();

        return new StoredEvent(
                LAST_ID.incrementAndGet(),
                event.getClass().getName(),
                event.toByteArray(),
                LocalDateTime.now());
    }

    public static StoredEvent createAnonymousStoredEvent_withStreamId() {
        Message event = createAnonymousDtoSample();

        return new StoredEvent(
                LAST_ID.incrementAndGet(),
                event.getClass().getName(),
                event.toByteArray(),
                LocalDateTime.now(),
                createAnonymousEventStreamId());
    }
}
