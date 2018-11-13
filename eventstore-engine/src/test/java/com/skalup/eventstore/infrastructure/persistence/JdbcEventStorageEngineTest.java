package com.skalup.eventstore.infrastructure.persistence;

import static com.marcowillemart.common.test.Tests.*;
import com.marcowillemart.eventstore.domain.EventData;
import com.marcowillemart.eventstore.domain.EventNotifiable;
import com.marcowillemart.eventstore.domain.EventStorageEngine;
import com.marcowillemart.eventstore.domain.StoredEventData;
import com.marcowillemart.eventstore.domain.WrongExpectedVersionException;
import com.marcowillemart.eventstore.infrastructure.persistence.JdbcEventStorageEngine;
import com.skalup.eventstore.test.TestConfig;
import static com.skalup.eventstore.test.Tests.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.sql.DataSource;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TestConfig.class)
@Transactional
public class JdbcEventStorageEngineTest {

    private static final String STREAM = createAnonymousName();

    private EventStorageEngine target;

    private EventNotifiable eventNotifiable;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Before
    public void setUp() {
        target = new JdbcEventStorageEngine(dataSource, transactionManager);

        eventNotifiable = mock(EventNotifiable.class);
    }

    @Test
    public void testCountStoredEvents() {
        // Setup
        target.append(createAnonymousEvent());
        target.append(createAnonymousEvent());

        // Exercise & Verify
        assertEquals(2, target.countStoredEvents());
    }

    @Test
    public void testCountStoredEvents_empty() {
        // Exercise & Verify
        assertEquals(0, target.countStoredEvents());
    }

    @Test
    public void testLastStoredEventId() {
        // Setup
        target.append(createAnonymousEvent());
        StoredEventData storedEvent = target.append(createAnonymousEvent());

        // Exercise & Verify
        assertEquals(storedEvent.eventId(), target.lastStoredEventId());
    }

    @Test
    public void testLastStoredEventId_empty() {
        // Exercise & Verify
        assertEquals(0, target.lastStoredEventId());
    }

    @Test
    public void testAllStoredEventsBetween() {
        // Setup
        target.append(createAnonymousEvent());
        StoredEventData evt1 = target.append(createAnonymousEvent());
        StoredEventData evt2 = target.append(createAnonymousEvent());
        StoredEventData evt3 = target.append(createAnonymousEvent());
        StoredEventData evt4 = target.append(createAnonymousEvent());
        target.append(createAnonymousEvent());

        // Exercise
        List<StoredEventData> actual =
                target.allStoredEventsBetween(
                        evt1.eventId(), evt4.eventId());

        // Verify
        List<StoredEventData> expected = Arrays.asList(evt1, evt2, evt3, evt4);
        assertEquals(expected, actual);
    }

    @Test
    public void testAllStoredEventsBetween_fromFirst() {
        // Setup
        StoredEventData evt1 = target.append(createAnonymousEvent());
        StoredEventData evt2 = target.append(createAnonymousEvent());
        StoredEventData evt3 = target.append(createAnonymousEvent());
        target.append(createAnonymousEvent());

        // Exercise
        List<StoredEventData> actual =
                target.allStoredEventsBetween(
                        evt1.eventId(),
                        evt3.eventId());

        // Verify
        List<StoredEventData> expected = Arrays.asList(evt1, evt2, evt3);
        assertEquals(expected, actual);
    }

    @Test
    public void testAllStoredEventsBetween_toLast() {
        // Setup
        target.append(createAnonymousEvent());
        StoredEventData evt2 = target.append(createAnonymousEvent());
        StoredEventData evt3 = target.append(createAnonymousEvent());
        StoredEventData evt4 = target.append(createAnonymousEvent());

        // Exercise
        List<StoredEventData> actual =
                target.allStoredEventsBetween(
                        evt2.eventId(),
                        evt4.eventId());

        // Verify
        List<StoredEventData> expected = Arrays.asList(evt2, evt3, evt4);
        assertEquals(expected, actual);
    }

    @Test
    public void testAllStoredEventsAfter() {
        // Setup
        target.append(createAnonymousEvent());
        StoredEventData evt1 = target.append(createAnonymousEvent());
        StoredEventData evt2 = target.append(createAnonymousEvent());
        StoredEventData evt3 = target.append(createAnonymousEvent());

        // Exercise
        List<StoredEventData> actual =
                target.allStoredEventsAfter(evt1.eventId());

        // Verify
        List<StoredEventData> expected = Arrays.asList(evt2, evt3);
        assertEquals(expected, actual);
    }

    @Test
    public void testEventStreamExists_true() {
        // Setup
        String streamName = setUpEventStream(1);

        // Exercise & Verify
        assertTrue(target.eventStreamExists(streamName));
    }

    @Test
    public void testEventStreamExists_false() {
        // Exercise & Verify
        assertFalse(target.eventStreamExists(STREAM));
    }

    @Test
    public void testLoadEventStream_notExists() {
        // Exercise & Verify
        assertTrue(target.loadEventStreamAfter(STREAM, 0).isEmpty());
    }

    @Test
    public void testLoadEventStream_oneEvent() {
        // Setup
        List<EventData> expecteds = createAnonymousEvents(1);
        setUpEventStream(STREAM, expecteds);

        // Exercise
        List<StoredEventData> actuals = target.loadEventStreamAfter(STREAM, 0);

        // Verify
        assertEventDataEquals(expecteds, actuals);
    }

    @Test
    public void testLoadEventStream_manyEvents() {
        // Setup
        List<EventData> expecteds = createAnonymousEvents(3);
        setUpEventStream(STREAM, expecteds);

        // Exercise
        List<StoredEventData> actuals = target.loadEventStreamAfter(STREAM, 0);

        // Verify
        assertEventDataEquals(expecteds, actuals);
    }

    @Test
    public void testLoadEventStreamAfter_zeroEvent() {
        // Setup
        int nbOfEvents = 10;
        List<EventData> events = createAnonymousEvents(10);
        setUpEventStream(STREAM, events);

        // Exercise
        List<StoredEventData> actuals =
                target.loadEventStreamAfter(STREAM, nbOfEvents);

        // Verify
        assertTrue(actuals.isEmpty());
    }

    @Test
    public void testLoadEventStreamAfter_oneEvent() {
        // Setup
        List<EventData> events = createAnonymousEvents(2);
        setUpEventStream(STREAM, events);

        // Exercise
        List<StoredEventData> actuals = target.loadEventStreamAfter(STREAM, 1);

        // Verify
        assertEquals(1, actuals.size());
        assertEventDataEquals(events.get(1), actuals.get(0));
    }

    @Test
    public void testLoadEventStreamAfter_manyEvents() {
        // Setup
        List<EventData> events = createAnonymousEvents(4);
        setUpEventStream(STREAM, events);

        // Exercise
        List<StoredEventData> actuals = target.loadEventStreamAfter(STREAM, 2);

        // Verify
        assertEventDataEquals(
                Arrays.asList(events.get(2), events.get(3)),
                actuals);
    }

    @Test
    public void testAppend() {
        // Setup
        EventData event = createAnonymousEvent();

        // Exercise
        StoredEventData actual = target.append(event);

        // Verify
        assertTrue(actual.eventId() > 0);
        assertEquals(event.type(), actual.eventType());
        assertArrayEquals(event.body(), actual.eventBody());
    }

    @Test
    public void testAppend_notifyDispatchableEvents() {
        // Setup
        target.register(eventNotifiable);

        // Exercise
        target.append(createAnonymousEvent());

        // Verify
        verify(eventNotifiable).notifyDispatchableEvents();
    }

    @Test
    public void testAppendToStream_newStream_zeroEvent() throws Exception {
        // Exercise
        target.appendToStream(STREAM, 0, Collections.<EventData>emptyList());

        // Verify
        assertEquals(0, target.countStoredEvents());
    }

    @Test
    public void testAppendToStream_newStream_oneEvent() throws Exception {
        // Setup
        EventData event = createAnonymousEvent();

        // Exercise
        target.appendToStream(STREAM, 0, Arrays.asList(event));

        // Verify
        assertEquals(1, target.countStoredEvents());
    }

    @Test
    public void testAppendToStream_newStream_manyEvents() throws Exception {
        // Setup
        EventData event1 = createAnonymousEvent();
        EventData event2 = createAnonymousEvent();

        // Exercise
        target.appendToStream(STREAM, 0, Arrays.asList(event1, event2));

        // Verify
        assertEquals(2, target.countStoredEvents());
    }

    @Test
    public void testAppendToStream_updateStream_zeroEvent() throws Exception {
        // Setup
        setUpEventStream(5);

        // Exercise
        target.appendToStream(STREAM, 5, Collections.<EventData>emptyList());

        // Verify
        assertEquals(5, target.countStoredEvents());
    }

    @Test
    public void testAppendToStream_updateStream_oneEvent() throws Exception {
        // Setup
        setUpEventStream(1);
        EventData event = createAnonymousEvent();

        // Exercise
        target.appendToStream(STREAM, 1, Arrays.asList(event));

        // Verify
        assertEquals(2, target.countStoredEvents());
    }

    @Test
    public void testAppendToStream_updateStream_manyEvents() throws Exception {
        // Setup
        setUpEventStream(2);
        EventData event1 = createAnonymousEvent();
        EventData event2 = createAnonymousEvent();

        // Exercise
        target.appendToStream(STREAM, 2, Arrays.asList(event1, event2));

        // Verify
        assertEquals(4, target.countStoredEvents());
    }

    @Test
    public void testAppendToStream_updateStream_manyEvents_bulkInsert() {
        // Setup
        String streamName = setUpEventStream(2);
        EventData event1 = createAnonymousEvent();
        EventData event2 = createAnonymousEvent();

        // Exercise & Verify
        try {
            target.appendToStream(streamName, 1, Arrays.asList(event1, event2));
            fail();
        } catch (WrongExpectedVersionException ex) {
            assertEquals(2, target.countStoredEvents());
        }
    }

    @Test
    public void testAppendToStream_concurrencyException_oneEvent() {
        // Setup
        EventData event1 = createAnonymousEvent();
        EventData event2 = createAnonymousEvent();

        String streamName = setUpEventStream(event1);

        // Exercise & Verify
        try {
            target.appendToStream(streamName, 0, Arrays.asList(event2));
            fail();
        } catch (WrongExpectedVersionException actual) {
            assertEquals(streamName, actual.streamName());
            assertEquals(0, actual.expectedVersion());
            assertEquals(1, actual.actualVersion());
            assertEquals(
                    Arrays.asList(eventOf(streamName, 1)),
                    actual.actualEvents());
            assertEquals(1, target.countStoredEvents());
        }
    }

    @Test
    public void testAppendToStream_concurrencyException_manyEvents() {
        // Setup
        EventData event1 = createAnonymousEvent();
        EventData event2 = createAnonymousEvent();
        EventData event3 = createAnonymousEvent();
        EventData event4 = createAnonymousEvent();
        EventData event5 = createAnonymousEvent();
        EventData event6 = createAnonymousEvent();

        String streamName = setUpEventStream(event1, event2, event3, event4);

        // Exercise & Verify
        try {
            target.appendToStream(streamName, 2, Arrays.asList(event5, event6));
            fail();
        } catch (WrongExpectedVersionException actual) {
            assertEquals(streamName, actual.streamName());
            assertEquals(2, actual.expectedVersion());
            assertEquals(4, actual.actualVersion());
            assertEquals(
                    Arrays.asList(
                            eventOf(streamName, 3),
                            eventOf(streamName, 4)),
                    actual.actualEvents());
            assertEquals(4, target.countStoredEvents());
        }
    }

    @Test
    public void testAppendToStream_notifyDispatchableEvents() throws Exception {
        // Setup
        target.register(eventNotifiable);

        // Exercise
        target.appendToStream(
                createAnonymousName(),
                0,
                createAnonymousEvents(2));

        // Verify
        verify(eventNotifiable).notifyDispatchableEvents();
    }

    ////////////////////
    // HELPER METHODS
    ////////////////////

    private String setUpEventStream(int numberOfEvents) {
        EventData[] events = new EventData[numberOfEvents];

        for (int i = 0; i < events.length; i++) {
            events[i] = createAnonymousEvent();
        }

        return setUpEventStream(events);
    }

    private String setUpEventStream(EventData... events) {
        String streamName = createAnonymousName();

        setUpEventStream(streamName, Arrays.asList(events));

        return streamName;
    }

    private void setUpEventStream(String streamName, List<EventData> events) {
        try {
            target.appendToStream(streamName, 0, events);
        } catch (WrongExpectedVersionException ex) {
            fail();
        }
    }

    private StoredEventData eventOf(String streamName, int eventNumber) {
        return target.loadEventStreamAfter(streamName, eventNumber - 1).get(0);
    }
}
