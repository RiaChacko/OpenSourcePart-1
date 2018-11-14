package com.marcowillemart.eventstore.infrastructure;

import com.google.protobuf.Message;
import com.marcowillemart.common.event.EventStore;
import com.marcowillemart.common.event.EventStoreConcurrencyException;
import com.marcowillemart.common.event.EventStream;
import com.marcowillemart.common.event.StoredEvent;
import com.marcowillemart.common.util.Assert;
import com.marcowillemart.eventstore.domain.EventData;
import com.marcowillemart.eventstore.domain.EventStorageEngine;
import com.marcowillemart.eventstore.domain.StoredEventData;
import com.marcowillemart.eventstore.domain.WrongExpectedVersionException;
import com.marcowillemart.eventstore.domain.subscription.SubscriptionManager;
import java.util.List;
import java.util.function.Consumer;

public final class JdbcEventStore implements EventStore {

    private final EventStorageEngine storageEngine;
    private final SubscriptionManager subscriptionManager;

    public JdbcEventStore(EventStorageEngine storageEngine) {

        Assert.notNull(storageEngine);

        this.storageEngine = storageEngine;
        this.subscriptionManager = new SubscriptionManager(this.storageEngine);
    }

    @Override
    public StoredEvent append(Message event) {
        Assert.notNull(event);

        EventData eventData = EventTranslator.toEventData(event);

        StoredEventData storedEventData = storageEngine.append(eventData);

        return EventTranslator.toStoredEvent(storedEventData);
    }

    @Override
    public void append(StoredEvent storedEvent) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long countStoredEvents() {
        return storageEngine.countStoredEvents();
    }

    @Override
    public long lastStoredEventId() {
        return storageEngine.lastStoredEventId();
    }

    @Override
    public List<StoredEvent> allStoredEventsBetween(
            long lowEventId,
            long highEventId) {

        List<StoredEventData> storedEventDatas =
                storageEngine.allStoredEventsBetween(lowEventId, highEventId);

        return EventTranslator.toStoredEvents(storedEventDatas);
    }

    @Override
    public List<StoredEvent> allStoredEventsSince(long eventId) {
        List<StoredEventData> storedEventDatas =
                storageEngine.allStoredEventsAfter(eventId - 1);

        return EventTranslator.toStoredEvents(storedEventDatas);
    }

    @Override
    public void appendToStream(
            String streamName,
            int expectedVersion,
            Iterable<Message> events) throws EventStoreConcurrencyException {

        Assert.notEmpty(streamName);
        Assert.isTrue(expectedVersion >= 0);
        Assert.notNull(events);

        List<EventData> eventDatas = EventTranslator.toEventDatas(events);

        try {
            storageEngine.appendToStream(
                    streamName,
                    expectedVersion,
                    eventDatas);
        } catch (WrongExpectedVersionException ex) {
            throw new EventStoreConcurrencyException(
                    ex.streamName(),
                    ex.expectedVersion(),
                    ex.actualVersion(),
                    EventTranslator.toMessages(ex.actualEvents()),
                    ex);
        }
    }

    @Override
    public boolean eventStreamExists(String streamName) {
        Assert.notNull(streamName);

        return storageEngine.eventStreamExists(streamName);
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

        List<StoredEventData> storedEventDatas =
                storageEngine.loadEventStreamAfter(streamName, version);

        return new EventStream(
                version + storedEventDatas.size(),
                EventTranslator.toMessages(storedEventDatas));
    }

    @Override
    public void subscribeToAll(
            long afterEventId,
            Consumer<Message> subscriber) {

        Assert.isTrue(afterEventId >= 0);
        Assert.notNull(subscriber);

        subscriptionManager.register(
                new EventSubscription(afterEventId, subscriber));
    }
}
