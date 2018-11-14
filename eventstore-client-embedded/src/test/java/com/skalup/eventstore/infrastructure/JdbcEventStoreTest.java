package com.marcowillemart.eventstore.infrastructure;

import com.google.protobuf.Message;
import com.marcowillemart.common.event.EventStore;
import com.marcowillemart.common.event.EventStoreConcurrencyException;
import com.marcowillemart.common.event.EventStream;
import com.marcowillemart.common.event.StoredEvent;
import static com.marcowillemart.common.test.Tests.*;
import com.marcowillemart.common.util.Lists;
import com.marcowillemart.eventstore.domain.EventStorageEngine;
import com.marcowillemart.eventstore.test.TestConfig;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TestConfig.class)
@Transactional
public class JdbcEventStoreTest {

    private static final String ID = createUUID();

    @Autowired
    private EventStorageEngine storageEngine;

    private EventStore target;

    @Before
    public void setUp() {
        target = new JdbcEventStore(storageEngine);
    }

    @Test
    public void testAppend_message() {
        // Setup
        Message message = createAnonymousMessage();

        // Exercise
        StoredEvent storedEvent = target.append(message);

        // Verify
        assertEquals(1, target.countStoredEvents());
        assertEquals(storedEvent, Lists.first(target.allStoredEventsSince(1)));
        assertEquals(message, storedEvent.toDomainEvent());
    }

    @Test
    public void testAppend_message_subscription() {
        // Setup
        List<Message> actuals = new LinkedList<>();
        target.subscribeToAll(0, actuals::add);

        Message event = createAnonymousMessage();

        // Exercise
        target.append(event);

        // Verify
        assertEquals(Arrays.asList(event), actuals);
    }

    @Test
    public void testCountStoredEvents_emptyStore() {
        // Exercise & Verify
        assertEquals(0, target.countStoredEvents());
    }

    @Test
    public void testCountStoredEvents() {
        // Setup
        target.append(createAnonymousMessage());
        target.append(createAnonymousMessage());

        // Exercise & Verify
        assertEquals(2, target.countStoredEvents());
    }

    @Test
    public void testLastStoredEventId_emptyStore() {
        // Exercise & Verify
        assertEquals(0, target.lastStoredEventId());
    }

    @Test
    public void testLastStoredEventId() {
        // Setup
        target.append(createAnonymousMessage());
        StoredEvent storedEvent = target.append(createAnonymousMessage());

        // Exercise & Verify
        assertEquals(storedEvent.eventId(), target.lastStoredEventId());
    }

    @Test
    public void testAllStoredEventsBetween() {
        // Setup
        target.append(createAnonymousMessage());
        StoredEvent evt1 = target.append(createAnonymousMessage());
        StoredEvent evt2 = target.append(createAnonymousMessage());
        StoredEvent evt3 = target.append(createAnonymousMessage());
        StoredEvent evt4 = target.append(createAnonymousMessage());
        target.append(createAnonymousMessage());

        // Exercise
        List<StoredEvent> actual =
                target.allStoredEventsBetween(
                        evt1.eventId(), evt4.eventId());

        // Verify
        List<StoredEvent> expected = Arrays.asList(evt1, evt2, evt3, evt4);
        assertEquals(expected, actual);
        actual.get(0).toDomainEvent();
    }

    @Test
    public void testAllStoredEventsSince() {
        // Setup
        target.append(createAnonymousMessage());
        StoredEvent evt1 = target.append(createAnonymousMessage());
        StoredEvent evt2 = target.append(createAnonymousMessage());
        StoredEvent evt3 = target.append(createAnonymousMessage());

        // Exercise
        List<StoredEvent> actual =
                target.allStoredEventsSince(evt1.eventId());

        // Verify
        List<StoredEvent> expected = Arrays.asList(evt1, evt2, evt3);
        assertEquals(expected, actual);
    }

    @Test
    public void testAppendToStream_newStream_oneEvent() {
        // Setup
        final Message event = createAnonymousMessage();

        // Exercise
        target.appendToStream(ID, 0, Arrays.asList(event));
    }

    @Test
    public void testAppendToStream_newStream_manyEvents() {
        // Setup
        final Message event1 = createAnonymousMessage();
        final Message event2 = createAnonymousMessage();

        // Exercise
        target.appendToStream(ID, 0, Arrays.asList(event1, event2));
    }

    @Test
    public void testAppendToStream_updateStream_zeroEvent() {
        // Setup
        setUpEvents(5);

        // Exercise
        target.appendToStream(ID, 5, Collections.<Message>emptyList());
    }

    @Test
    public void testAppendToStream_updateStream_oneEvent() {
        // Setup
        setUpEvents(1);
        final Message event = createAnonymousMessage();

        // Exercise
        target.appendToStream(ID, 1, Arrays.asList(event));
    }

    @Test
    public void testAppendToStream_updateStream_manyEvents() {
        // Setup
        setUpEvents(2);
        final Message event1 = createAnonymousMessage();
        final Message event2 = createAnonymousMessage();

        // Exercise
        target.appendToStream(ID, 2, Arrays.asList(event1, event2));
    }

    @Test
    public void testAppendToStream_updateStream_manyEvents_bulkInsert() {
        // Setup
        setUpEvents(2);
        final Message event1 = createAnonymousMessage();
        final Message event2 = createAnonymousMessage();

        // Exercise
        try {
            target.appendToStream(ID, 1, Arrays.asList(event1, event2));
            fail();
        } catch (EventStoreConcurrencyException expected) {
            assertEquals(2, target.countStoredEvents());
        }
    }

    @Test
    public void testAppendToStream_concurrencyException_oneEvent() {
        // Setup
        final Message actualEvent = createAnonymousMessage();
        setUpEvents(actualEvent);
        final Message event = createAnonymousMessage();
        final int expectedVersion = 0;
        final int actualVersion = 1;

        // Exercise
        try {
            target.appendToStream(ID, expectedVersion, Arrays.asList(event));
            fail();
        } catch (EventStoreConcurrencyException ex) {
            assertEquals(ID, ex.name());
            assertEquals(expectedVersion, ex.expectedVersion());
            assertEquals(actualVersion, ex.actualVersion());
            assertEquals(Arrays.asList(actualEvent), ex.actualEvents());
        }
    }

    @Test
    public void testAppendToStream_concurrencyException_manyEvents() {
        // Setup
        final Message actualEvent1 = createAnonymousMessage();
        final Message actualEvent2 = createAnonymousMessage();
        setUpEvents(
                createAnonymousMessage(),
                createAnonymousMessage(),
                actualEvent1,
                actualEvent2);
        final Message event1 = createAnonymousMessage();
        final Message event2 = createAnonymousMessage();
        final int expectedVersion = 2;
        final int actualVersion = 4;

        // Exercise
        try {
            target.appendToStream(
                    ID,
                    expectedVersion,
                    Arrays.asList(event1, event2));
            fail();
        } catch (EventStoreConcurrencyException ex) {
            assertEquals(ID, ex.name());
            assertEquals(expectedVersion, ex.expectedVersion());
            assertEquals(actualVersion, ex.actualVersion());
            assertEquals(
                    Arrays.asList(actualEvent1, actualEvent2),
                    ex.actualEvents());
        }
    }

    @Test
    public void testAppendToStream_subscription() {
        // Setup
        List<Message> actuals = new LinkedList<>();
        target.subscribeToAll(0, actuals::add);

        Message event1 = createAnonymousMessage();
        Message event2 = createAnonymousMessage();
        Message event3 = createAnonymousMessage();

        // Exercise
        target.appendToStream(ID, 0, Arrays.asList(event1, event2, event3));

        // Verify
        assertEquals(Arrays.asList(event1, event2, event3), actuals);
    }

    @Test
    public void testEventStreamExists_true() {
        // Setup
        setUpEvents(1);

        // Exercise & Verify
        assertTrue(target.eventStreamExists(ID));
    }

    @Test
    public void testEventStreamExists_false() {
        // Exercise & Verify
        assertFalse(target.eventStreamExists(ID));
    }

    @Test
    public void testLoadEventStream_oneEvent() {
        // Setup
        Message event = createAnonymousMessage();
        EventStream expected = new EventStream(1, Arrays.asList(event));
        setUpEvents(event);

        // Exercise
        EventStream actual = target.loadEventStream(ID);

        // Verify
        assertEquals(expected, actual);
    }

    @Test
    public void testLoadEventStream_manyEvents() {
        // Setup
        Message event1 = createAnonymousMessage();
        Message event2 = createAnonymousMessage();
        Message event3 = createAnonymousMessage();
        EventStream expected =
                new EventStream(
                        3, Arrays.asList(event1, event2, event3));
        setUpEvents(event1, event2, event3);

        // Exercise
        EventStream actual = target.loadEventStream(ID);

        // Verify
        assertEquals(expected, actual);
    }

    @Test
    public void testLoadEventStream_notExists() {
        // Exercise
        EventStream eventStream = target.loadEventStream(ID);

        // Verify
        assertEquals(0, eventStream.version());
        assertTrue(eventStream.events().isEmpty());
    }

    @Test
    public void testLoadEventStreamAfter_zeroEvent() {
        // Setup
        int nbOfEvents = 10;
        setUpEvents(nbOfEvents);
        EventStream expected =
                new EventStream(nbOfEvents, Collections.<Message>emptyList());

        // Exercise
        EventStream actual = target.loadEventStreamAfter(ID, nbOfEvents);

        // Verify
        assertEquals(expected, actual);
    }

    @Test
    public void testLoadEventStreamAfter_oneEvent() {
        // Setup
        Message event = createAnonymousMessage();
        setUpEvents(createAnonymousMessage(), event);
        EventStream expected = new EventStream(2, Arrays.asList(event));

        // Exercise
        EventStream actual = target.loadEventStreamAfter(ID, 1);

        // Verify
        assertEquals(expected, actual);
    }

    @Test
    public void testLoadEventStreamAfter_manyEvents() {
        // Setup
        Message event1 = createAnonymousMessage();
        Message event2 = createAnonymousMessage();
        EventStream expected =
                new EventStream(4, Arrays.asList(event1, event2));
        setUpEvents(
                createAnonymousMessage(),
                createAnonymousMessage(),
                event1,
                event2);

        // Exercise
        EventStream actual = target.loadEventStreamAfter(ID, 2);

        // Verify
        assertEquals(expected, actual);
    }

    @Test
    public void testSubscribeToAll_emptyStore() {
        // Exercise
        target.subscribeToAll(0, event -> fail());
    }

    @Test
    public void testSubscribeToAll_afterLastEvent() {
        // Setup
        setUpEvents(2);

        // Exercise
        target.subscribeToAll(target.lastStoredEventId(), event -> fail());
    }

    @Test
    public void testSubscribeToAll_beforeFirstEvent() {
        // Setup
        Message event1 = createAnonymousMessage();
        Message event2 = createAnonymousMessage();
        setUpEvents(event1, event2);
        List<Message> actuals = new LinkedList<>();

        // Exercise
        target.subscribeToAll(0, actuals::add);

        // Verify
        assertEquals(Arrays.asList(event1, event2), actuals);
    }

    @Test
    public void testSubscribeToAll_beforeLastEvent() {
        // Setup
        Message event1 = createAnonymousMessage();
        Message event2 = createAnonymousMessage();

        long eventId1 = target.append(event1).eventId();
        target.append(event2);

        List<Message> actuals = new LinkedList<>();

        // Exercise
        target.subscribeToAll(eventId1, actuals::add);

        // Verify
        assertEquals(Arrays.asList(event2), actuals);
    }

    ////////////////////
    // HELPER METHODS
    ////////////////////

    private void setUpEvents(int numberOfEvents) {
        Message[] events = new Message[numberOfEvents];

        for (int i = 0; i < events.length; i++) {
            events[i] = createAnonymousMessage();
        }

        setUpEvents(events);
    }

    private void setUpEvents(final Message... events) {
        target.appendToStream(ID, 0, Arrays.asList(events));
    }
}
