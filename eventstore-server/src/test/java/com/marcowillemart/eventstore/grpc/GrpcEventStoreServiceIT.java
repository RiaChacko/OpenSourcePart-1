package com.marcowillemart.eventstore.grpc;

import static com.marcowillemart.common.test.Tests.*;
import com.marcowillemart.eventstore.AppendToStreamRequest;
import com.marcowillemart.eventstore.AppendToStreamResponse;
import com.marcowillemart.eventstore.EventStreamExistsRequest;
import com.marcowillemart.eventstore.EventStreamExistsResponse;
import com.marcowillemart.eventstore.LoadEventStreamRequest;
import com.marcowillemart.eventstore.LoadEventStreamResponse;
import com.marcowillemart.eventstore.StoredEventData;
import com.marcowillemart.eventstore.SubscribeToAllRequest;
import com.marcowillemart.eventstore.test.AbstractIntegrationTest;
import static com.marcowillemart.eventstore.test.Tests.*;
import io.grpc.stub.StreamObserver;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.Test;

public class GrpcEventStoreServiceIT extends AbstractIntegrationTest {

    @Test
    public void testAppendToStream_newStream_oneEvent() {
        // Exercise
        AppendToStreamResponse actual =
                client.appendToStream(
                        AppendToStreamRequest.newBuilder()
                                .setStreamName(createAnonymousName())
                                .setExpectedVersion(0)
                                .addEvents(createAnonymousEventData())
                                .build());

        // Verify
        AppendToStreamResponse expected =
                AppendToStreamResponse.newBuilder()
                        .setSuccess(true)
                        .build();
        assertEquals(expected, actual);
    }

    @Test
    public void testAppendToStream_newStream_manyEvents() {
        // Exercise
        AppendToStreamResponse actual =
                client.appendToStream(
                        AppendToStreamRequest.newBuilder()
                                .setStreamName(createAnonymousName())
                                .setExpectedVersion(0)
                                .addEvents(createAnonymousEventData())
                                .addEvents(createAnonymousEventData())
                                .build());

        // Verify
        AppendToStreamResponse expected =
                AppendToStreamResponse.newBuilder()
                        .setSuccess(true)
                        .build();
        assertEquals(expected, actual);
    }

    @Test
    public void testAppendToStream_existingStream_oneEvent() {
        // Setup
        String streamName = createAnonymousName();
        appendEventsToStream(streamName, 1);

        // Exercise
        AppendToStreamResponse actual =
                client.appendToStream(
                        AppendToStreamRequest.newBuilder()
                                .setStreamName(createAnonymousName())
                                .setExpectedVersion(1)
                                .addEvents(createAnonymousEventData())
                                .build());

        // Verify
        AppendToStreamResponse expected =
                AppendToStreamResponse.newBuilder()
                        .setSuccess(true)
                        .build();
        assertEquals(expected, actual);
    }

    @Test
    public void testAppendToStream_existingStream_manyEvents() {
        // Setup
        String streamName = createAnonymousName();
        appendEventsToStream(streamName, 2);

        // Exercise
        AppendToStreamResponse actual =
                client.appendToStream(
                        AppendToStreamRequest.newBuilder()
                                .setStreamName(createAnonymousName())
                                .setExpectedVersion(2)
                                .addEvents(createAnonymousEventData())
                                .addEvents(createAnonymousEventData())
                                .build());

        // Verify
        AppendToStreamResponse expected =
                AppendToStreamResponse.newBuilder()
                        .setSuccess(true)
                        .build();
        assertEquals(expected, actual);
    }

    @Test
    public void testEventStreamExists_true() {
        // Setup
        String streamName = createAnonymousName();
        appendEventsToStream(streamName, 1);

        // Exercise
        EventStreamExistsResponse response =
                client.eventStreamExists(
                        EventStreamExistsRequest.newBuilder()
                                .setStreamName(streamName)
                                .build());

        // Verify
        assertTrue(response.getStreamExists());
    }

    @Test
    public void testEventStreamExists_false() {
        // Setup
        String streamName = createAnonymousName();

        // Exercise
        EventStreamExistsResponse response =
                client.eventStreamExists(
                        EventStreamExistsRequest.newBuilder()
                                .setStreamName(streamName)
                                .build());

        // Verify
        assertFalse(response.getStreamExists());
    }

    @Test
    public void testLoadEventStream_oneEvent() {
        // Setup
        String streamName = createAnonymousName();
        appendEventsToStream(streamName, 1);

        // Exercise
        LoadEventStreamResponse response =
                client.loadEventStream(
                        LoadEventStreamRequest.newBuilder()
                                .setStreamName(streamName)
                                .setAfterStreamVersion(0)
                                .build());

        // Verify
        assertEquals(1, response.getStreamVersion());
        assertEquals(1, response.getEventsCount());
    }

    @Test
    public void testSubscribeToAll() throws Exception {
        // Setup
        List<StoredEventData> events = new LinkedList<>();
        events.addAll(appendEvents(2));
        events.addAll(appendEventsToStream(3));

        TestStreamObserver observer1 = new TestStreamObserver();
        TestStreamObserver observer2 = new TestStreamObserver();

        // Exercise
        asyncClient.subscribeToAll(
                afterEventId(events.get(0).getEventId() - 1),
                observer1);
        asyncClient.subscribeToAll(
                afterEventId(events.get(3).getEventId() - 1),
                observer2);

        // Verify
        events.addAll(appendEvents(4));
        events.addAll(appendEventsToStream(5));

        assertEquals(events, observer1.deliveredEvents());
        assertEquals(
                events.subList(3, events.size()),
                observer2.deliveredEvents());
    }

    ////////////////////
    // HELPER METHODS
    ////////////////////

    private List<StoredEventData> appendEvents(int nbOfEvents) {
        List<StoredEventData> storedEvents = new ArrayList<>(nbOfEvents);

        createAnonymousEvents(nbOfEvents)
                .forEach(event -> storedEvents.add(client.append(event)));

        return storedEvents;
    }

    private List<StoredEventData> appendEventsToStream(int nbOfEvents) {
        return appendEventsToStream(createAnonymousName(), nbOfEvents);
    }

    private List<StoredEventData> appendEventsToStream(
            String streamName,
            int nbOfEvents) {

        client.appendToStream(
                AppendToStreamRequest.newBuilder()
                        .setStreamName(streamName)
                        .setExpectedVersion(0)
                        .addAllEvents(createAnonymousEvents(nbOfEvents))
                        .build());

        return client.loadEventStream(
                LoadEventStreamRequest.newBuilder()
                        .setStreamName(streamName)
                        .build())
                .getEventsList();
    }

    private static SubscribeToAllRequest afterEventId(long afterEventId) {
        return SubscribeToAllRequest.newBuilder()
                        .setAfterEventId(afterEventId)
                        .build();
    }

    ////////////////////
    // INNER CLASSES
    ////////////////////

    private static class TestStreamObserver
            implements StreamObserver<StoredEventData> {

        private final List<StoredEventData> deliveredEvents = new LinkedList<>();

        public List<StoredEventData> deliveredEvents() {
            return Collections.unmodifiableList(deliveredEvents);
        }

        @Override
        public void onNext(StoredEventData storedEvent) {
            deliveredEvents.add(storedEvent);
        }

        @Override
        public void onError(Throwable t) {
            fail();
        }

        @Override
        public void onCompleted() {
            fail();
        }
    } // end TestStreamObserver
}
