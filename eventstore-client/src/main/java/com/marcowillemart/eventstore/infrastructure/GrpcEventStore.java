package com.marcowillemart.eventstore.infrastructure;

import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import com.marcowillemart.common.event.EventStore;
import com.marcowillemart.common.event.EventStoreConcurrencyException;
import com.marcowillemart.common.event.EventStream;
import com.marcowillemart.common.event.StoredEvent;
import com.marcowillemart.common.util.Assert;
import com.marcowillemart.eventstore.AllStoredEventsRequest;
import com.marcowillemart.eventstore.AllStoredEventsResponse;
import com.marcowillemart.eventstore.AppendToStreamRequest;
import com.marcowillemart.eventstore.AppendToStreamResponse;
import com.marcowillemart.eventstore.CountStoredEventsResponse;
import com.marcowillemart.eventstore.EventData;
import com.marcowillemart.eventstore.EventStoreGrpc;
import com.marcowillemart.eventstore.EventStoreGrpc.EventStoreBlockingStub;
import com.marcowillemart.eventstore.EventStoreGrpc.EventStoreStub;
import com.marcowillemart.eventstore.EventStreamExistsRequest;
import com.marcowillemart.eventstore.EventStreamExistsResponse;
import com.marcowillemart.eventstore.LastStoredEventIdResponse;
import com.marcowillemart.eventstore.LoadEventStreamRequest;
import com.marcowillemart.eventstore.LoadEventStreamResponse;
import com.marcowillemart.eventstore.StoredEventData;
import com.marcowillemart.eventstore.SubscribeToAllRequest;
import com.marcowillemart.eventstore.WrongExpectedVersion;
import com.marcowillemart.grpc.client.GrpcChannelFactory;
import io.grpc.Channel;
import io.grpc.stub.StreamObserver;
import java.util.List;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GrpcEventStore implements EventStore {

    private static final Logger LOG =
            LoggerFactory.getLogger(GrpcEventStore.class);

    private final EventStoreBlockingStub client;
    private final EventStoreStub asyncClient;

    public GrpcEventStore(GrpcChannelFactory channelFactory) {
        Assert.notNull(channelFactory);

        Channel channel = channelFactory.createChannel("default");

        this.client = EventStoreGrpc.newBlockingStub(channel);
        this.asyncClient = EventStoreGrpc.newStub(channel);
    }

    @Override
    public StoredEvent append(Message event) {
        Assert.notNull(event);

        EventData eventData = EventTranslator.toEventData(event);

        StoredEventData storedEventData = client.append(eventData);

        return EventTranslator.toStoredEvent(storedEventData);
    }

    @Override
    public void append(StoredEvent storedEvent) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long countStoredEvents() {
        CountStoredEventsResponse response =
                client.countStoredEvents(Empty.getDefaultInstance());

        return response.getCount();
    }

    @Override
    public long lastStoredEventId() {
        LastStoredEventIdResponse response =
                client.lastStoredEventId(Empty.getDefaultInstance());

        return response.getLastEventId();
    }

    @Override
    public List<StoredEvent> allStoredEventsBetween(
            long lowEventId,
            long highEventId) {

        AllStoredEventsRequest request =
                AllStoredEventsRequest.newBuilder()
                        .setLowEventId(lowEventId)
                        .setHighEventId(highEventId)
                        .build();

        AllStoredEventsResponse response = client.allStoredEvents(request);

        return EventTranslator.toStoredEvents(response.getEventsList());
    }

    @Override
    public List<StoredEvent> allStoredEventsSince(long eventId) {
        return allStoredEventsBetween(eventId, lastStoredEventId());
    }

    @Override
    public void appendToStream(
            String streamName,
            int expectedVersion,
            Iterable<Message> events) throws EventStoreConcurrencyException {

        Assert.notEmpty(streamName);
        Assert.isTrue(expectedVersion >= 0);
        Assert.notNull(events);

        AppendToStreamRequest request =
                AppendToStreamRequest.newBuilder()
                        .setStreamName(streamName)
                        .setExpectedVersion(expectedVersion)
                        .addAllEvents(EventTranslator.toEventDatas(events))
                        .build();

        AppendToStreamResponse response = client.appendToStream(request);

        if (response.hasWrongExpectedVersion()) {
            WrongExpectedVersion wrongExpectedVersion =
                    response.getWrongExpectedVersion();

            throw new EventStoreConcurrencyException(
                    wrongExpectedVersion.getStreamName(),
                    wrongExpectedVersion.getExpectedVersion(),
                    wrongExpectedVersion.getActualVersion(),
                    EventTranslator.toMessages(
                            wrongExpectedVersion.getActualEventsList()));
        }
    }

    @Override
    public boolean eventStreamExists(String streamName) {
        Assert.notNull(streamName);

        EventStreamExistsRequest request =
                EventStreamExistsRequest.newBuilder()
                        .setStreamName(streamName)
                        .build();

        EventStreamExistsResponse response = client.eventStreamExists(request);

        return response.getStreamExists();
    }

    @Override
    public EventStream loadEventStream(String streamName) {
        Assert.notNull(streamName);

        return loadEventStreamAfter(streamName, 0);
    }

    @Override
    public EventStream loadEventStreamAfter(String streamName, int version) {
        Assert.notNull(streamName);
        Assert.isTrue(version >= 0);

        LoadEventStreamRequest request =
                LoadEventStreamRequest.newBuilder()
                        .setStreamName(streamName)
                        .setAfterStreamVersion(version)
                        .build();

        LoadEventStreamResponse response = client.loadEventStream(request);

        return new EventStream(
                response.getStreamVersion(),
                EventTranslator.toMessages(response.getEventsList()));
    }

    @Override
    public void subscribeToAll(
            long afterEventId,
            Consumer<Message> subscriber) {

        Assert.isTrue(afterEventId >= 0);
        Assert.notNull(subscriber);

        SubscribeToAllRequest request =
                SubscribeToAllRequest.newBuilder()
                        .setAfterEventId(afterEventId)
                        .build();

        asyncClient.subscribeToAll(
                request,
                new StreamObserver<StoredEventData>() {

            @Override
            public void onNext(StoredEventData storedEventData) {
                subscriber.accept(EventTranslator.toMessage(storedEventData));
            }

            @Override
            public void onError(Throwable error) {
                // TODO: Handle error properly with retry
                LOG.error(error.getMessage(), error);
            }

            @Override
            public void onCompleted() {
                Assert.shouldNeverGetHere();
                // TODO: not sure the thrown exception will arrive at the top
                LOG.error("Should never get here");
            }
        });
    }
}
