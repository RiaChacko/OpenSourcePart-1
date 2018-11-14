package com.marcowillemart.eventstore.infrastructure;

import com.google.protobuf.Message;
import com.marcowillemart.common.event.StoredEvent;
import com.marcowillemart.common.util.Assert;
import com.marcowillemart.common.util.MessageSerializer;
import com.marcowillemart.eventstore.domain.EventData;
import com.marcowillemart.eventstore.domain.StoredEventData;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

final class EventTranslator {

    private EventTranslator() {
        Assert.shouldNeverGetHere();
    }

    static EventData toEventData(Message event) {
        return new EventData(event.getClass().getName(), event.toByteArray());
    }

    static List<EventData> toEventDatas(Iterable<Message> events) {
        List<EventData> eventDataList = new LinkedList<>();

        events.forEach(event -> eventDataList.add(toEventData(event)));

        return eventDataList;
    }

    static StoredEvent toStoredEvent(StoredEventData storedEventData) {
        return new StoredEvent(
                storedEventData.eventId(),
                storedEventData.eventType(),
                storedEventData.eventBody(),
                storedEventData.occurredOn());
    }

    static List<StoredEvent> toStoredEvents(
            List<StoredEventData> storedEventDatas) {

        return storedEventDatas
                .stream()
                .map(EventTranslator::toStoredEvent)
                .collect(Collectors.toList());
    }

    static Message toMessage(StoredEventData storedEventData) {
        return MessageSerializer.deserialize(
                storedEventData.eventType(),
                storedEventData.eventBody());
    }

    static List<Message> toMessages(List<StoredEventData> storedEventDatas) {
        return storedEventDatas
                .stream()
                .map(EventTranslator::toMessage)
                .collect(Collectors.toList());
    }
}
