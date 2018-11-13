package com.skalup.eventstore.test;

import com.google.protobuf.Message;
import static com.marcowillemart.common.test.Tests.*;
import com.marcowillemart.eventstore.domain.EventData;
import com.marcowillemart.eventstore.domain.StoredEventData;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;

public final class Tests {

    private Tests() {
        throw new AssertionError();
    }

    ////////////////////
    // CREATION METHODS
    ////////////////////

    ////////////////////
    // UTILITY METHODS
    ////////////////////

    public static EventData createAnonymousEvent() {
        Message message = createAnonymousMessage();

        return new EventData(
                message.getClass().getName(),
                message.toByteArray());
    }

    public static List<EventData> createAnonymousEvents(int nbOfEvents) {
        List<EventData> events = new ArrayList<>(nbOfEvents);

        for (int i = 0; i < nbOfEvents; i++) {
            events.add(createAnonymousEvent());
        }

        return events;
    }

    ////////////////////
    // CUSTOM ASSERTIONS
    ////////////////////

    public static void assertEventDataEquals(
            EventData expected,
            StoredEventData actual) {

        assertEquals(expected.type(), actual.eventType());
        assertArrayEquals(expected.body(), actual.eventBody());
    }

    public static void assertEventDataEquals(
            List<EventData> expecteds,
            List<StoredEventData> actuals) {

        assertEquals(expecteds.size(), actuals.size());

        for (int i = 0; i < expecteds.size(); i++) {
            assertEventDataEquals(expecteds.get(i), actuals.get(i));
        }
    }

    ////////////////////
    // HELPER METHODS
    ////////////////////
}
