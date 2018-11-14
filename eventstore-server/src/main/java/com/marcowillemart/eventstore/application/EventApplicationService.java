package com.marcowillemart.eventstore.application;

import com.marcowillemart.common.util.Assert;
import com.marcowillemart.eventstore.domain.EventData;
import com.marcowillemart.eventstore.domain.EventStorageEngine;
import com.marcowillemart.eventstore.domain.StoredEventData;
import com.marcowillemart.eventstore.domain.WrongExpectedVersionException;
import com.marcowillemart.eventstore.grpc.ProtoTranslator;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EventApplicationService {

    private static final Logger LOG =
            LoggerFactory.getLogger(EventApplicationService.class);

    private final EventStorageEngine storageEngine;

    public EventApplicationService(EventStorageEngine storageEngine) {
        Assert.notNull(storageEngine);

        this.storageEngine = storageEngine;
    }

    public com.marcowillemart.eventstore.StoredEventData append(
            com.marcowillemart.eventstore.EventData eventDataProto) {

        EventData eventData = ProtoTranslator.toEventData(eventDataProto);

        StoredEventData storedEventData = storageEngine.append(eventData);

        LOG.info("Event '{}' appended", storedEventData.eventId());

        return ProtoTranslator.toProto(storedEventData);
    }

    public Optional<com.marcowillemart.eventstore.WrongExpectedVersion>
        appendToStream(
            String streamName,
            int expectedVersion,
            List<com.marcowillemart.eventstore.EventData> eventProtos) {

        List<EventData> eventDatas = ProtoTranslator.toEventDatas(eventProtos);

        try {
            storageEngine.appendToStream(
                    streamName,
                    expectedVersion,
                    eventDatas);

            LOG.info("{} events appended to v{} of stream '{}'",
                    eventProtos.size(),
                    expectedVersion,
                    streamName);

            return Optional.empty();
        } catch (WrongExpectedVersionException ex) {
            LOG.warn(ex.getMessage());

            return Optional.of(ProtoTranslator.toProto(ex));
        }
    }
}
