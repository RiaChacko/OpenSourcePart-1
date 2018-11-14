package com.marcowillemart.eventstore.infrastructure;

import com.google.protobuf.Message;
import com.marcowillemart.common.event.EventStreamId;
import com.marcowillemart.common.event.StoredEvent;
import com.marcowillemart.common.util.Assert;
import com.marcowillemart.common.util.MessageSerializer;
import com.marcowillemart.common.util.Timestamps;
import com.marcowillemart.eventstore.EventData;
import com.marcowillemart.eventstore.StoredEventData;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

final class EventTranslator {

    private EventTranslator() {
        Assert.shouldNeverGetHere();
    }

    static EventData toEventData(Message event) {
        return EventData.newBuilder()
                .setType(event.getClass().getName())
                .setBody(event.toByteString())
                .build();
    }

    static List<EventData> toEventDatas(Iterable<Message> events) {
        List<EventData> eventDatas = new LinkedList<>();

        events.forEach(event -> eventDatas.add(toEventData(event)));

        return eventDatas;
    }

    static StoredEvent toStoredEvent(StoredEventData storedEventData) {
        return new StoredEvent(
                storedEventData.getEventId(),
                storedEventData.getEventType(),
                storedEventData.getEventBody().toByteArray(),
                Timestamps.toLocalDateTime(storedEventData.getOccurredOn()),
                optionalStreamIdOf(storedEventData));
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
                storedEventData.getEventType(),
                storedEventData.getEventBody().toByteArray());
    }

    static List<Message> toMessages(List<StoredEventData> storedEventDatas) {
        return storedEventDatas
                .stream()
                .map(EventTranslator::toMessage)
                .collect(Collectors.toList());
    }

    ////////////////////
    // HELPER METHODS
    ////////////////////

   private static EventStreamId optionalStreamIdOf(
           StoredEventData storedEventData) {

       if (storedEventData.getStreamName().isEmpty()) {
           return null;
       }

       return new EventStreamId(
               storedEventData.getStreamName(),
               storedEventData.getStreamVersion());
   }
}
