package com.marcowillemart.eventstore.application;

import com.marcowillemart.eventstore.domain.EventStorageEngine;
import com.marcowillemart.eventstore.domain.StoredEventData;
import com.marcowillemart.eventstore.grpc.ProtoTranslator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EventQueryService {

    private static final Logger LOG =
            LoggerFactory.getLogger(EventQueryService.class);

    @Autowired
    private EventStorageEngine eventStorageEngine;

    public EventQueryService() {
    }

    public long countStoredEvents() {
        long count = eventStorageEngine.countStoredEvents();

        LOG.info("{} events counted", count);

        return count;
    }

    public long lastStoredEventId() {
        long lastEventId = eventStorageEngine.lastStoredEventId();

        LOG.info("Event {} last stored", lastEventId);

        return lastEventId;
    }

    public List<com.marcowillemart.eventstore.StoredEventData> allStoredEvents(
            long lowEventId,
            long highEventId) {

        List<StoredEventData> storedEvents =
                eventStorageEngine.allStoredEventsBetween(
                        lowEventId,
                        highEventId);

        LOG.info("{} events read between events '{}' and '{}'",
                storedEvents,
                lowEventId,
                highEventId);

        return ProtoTranslator.toProtos(storedEvents);
    }

    public List<com.marcowillemart.eventstore.StoredEventData>
        allStoredEventsAfter(long eventId) {

        List<StoredEventData> storedEvents =
                eventStorageEngine.allStoredEventsAfter(eventId);

        return ProtoTranslator.toProtos(storedEvents);
    }

    public boolean eventStreamExists(String streamName) {
        return eventStorageEngine.eventStreamExists(streamName);
    }

    public List<com.marcowillemart.eventstore.StoredEventData>
        loadEventStreamAfter(String streamName, int version) {

        List<StoredEventData> storedEvents =
                eventStorageEngine.loadEventStreamAfter(
                        streamName,
                        version);

        LOG.info("Event stream '{}' loaded from v{} to v{}",
                streamName,
                version + 1,
                version + storedEvents.size());

        return ProtoTranslator.toProtos(storedEvents);
    }
}
