package com.marcowillemart.eventstore.grpc;

import com.google.protobuf.ByteString;
import com.marcowillemart.common.util.Assert;
import com.marcowillemart.common.util.Timestamps;
import com.marcowillemart.eventstore.domain.EventData;
import com.marcowillemart.eventstore.domain.StoredEventData;
import com.marcowillemart.eventstore.domain.WrongExpectedVersionException;
import java.util.List;
import java.util.stream.Collectors;

public final class ProtoTranslator {

    private ProtoTranslator() {
        Assert.shouldNeverGetHere();
    }

    public static EventData toEventData(
            com.marcowillemart.eventstore.EventData eventProto) {
        
        return new EventData(
                eventProto.getType(),
                eventProto.getBody().toByteArray());
    }

    public static List<EventData> toEventDatas(
            List<com.marcowillemart.eventstore.EventData> eventProtos) {

        return eventProtos
                .stream()
                .map(ProtoTranslator::toEventData)
                .collect(Collectors.toList());
    }

    public static com.marcowillemart.eventstore.StoredEventData toProto(
            StoredEventData storedEvent) {

        return com.marcowillemart.eventstore.StoredEventData.newBuilder()
                .setEventId(storedEvent.eventId())
                .setEventType(storedEvent.eventType())
                .setEventBody(ByteString.copyFrom(storedEvent.eventBody()))
                .setOccurredOn(Timestamps.toTimeStamp(storedEvent.occurredOn()))
                .setStreamName(storedEvent.streamName())
                .setStreamVersion(storedEvent.streamVersion())
                .build();
    }

    public static List<com.marcowillemart.eventstore.StoredEventData> toProtos(
            List<StoredEventData> storedEvents) {

        return storedEvents
                .stream()
                .map(ProtoTranslator::toProto)
                .collect(Collectors.toList());
    }

    public static com.marcowillemart.eventstore.WrongExpectedVersion toProto(
            WrongExpectedVersionException exception) {

        return com.marcowillemart.eventstore.WrongExpectedVersion.newBuilder()
                .setStreamName(exception.streamName())
                .setExpectedVersion(exception.expectedVersion())
                .setActualVersion(exception.actualVersion())
                .addAllActualEvents(toProtos(exception.actualEvents()))
                .build();
    }
}
