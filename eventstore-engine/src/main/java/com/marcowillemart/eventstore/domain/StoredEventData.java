package com.marcowillemart.eventstore.domain;

import com.marcowillemart.common.util.Assert;
import com.marcowillemart.common.util.Strings;
import java.time.LocalDateTime;
import java.util.Arrays;

public final class StoredEventData {

    private final long eventId;
    private final String eventType;
    private final byte[] eventBody;
    private final LocalDateTime occurredOn;
    private final String streamName;
    private final int streamVersion;

    public StoredEventData(
            long eventId,
            String eventType,
            byte[] eventBody,
            LocalDateTime occurredOn) {
        this(eventId, eventType, eventBody, occurredOn, Strings.EMPTY, 0);
    }

    public StoredEventData(
            long eventId,
            String eventType,
            byte[] eventBody,
            LocalDateTime occurredOn,
            String streamName,
            int streamVersion) {

        Assert.isTrue(eventId > 0L);
        Assert.notEmpty(eventType);
        Assert.notNull(eventBody);
        Assert.notNull(occurredOn);
        Assert.isFalse(occurredOn.isAfter(LocalDateTime.now()));
        Assert.notNull(streamName);
        Assert.equiv(streamName.isEmpty(), streamVersion == 0);
        Assert.equiv(!streamName.isEmpty(), streamVersion > 0);

        this.eventId = eventId;
        this.eventType = eventType;
        this.eventBody = eventBody.clone();
        this.occurredOn = occurredOn;
        this.streamName = streamName;
        this.streamVersion = streamVersion;
    }

    public long eventId() {
        return eventId;
    }

    public String eventType() {
        return eventType;
    }

    public byte[] eventBody() {
        return eventBody.clone();
    }

    public LocalDateTime occurredOn() {
        return occurredOn;
    }

    public String streamName() {
        return streamName;
    }

    public int streamVersion() {
        return streamVersion;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (int) (eventId ^ (eventId >>> 32));
        hash = 37 * hash + eventType.hashCode();
        hash = 37 * hash + Arrays.hashCode(eventBody);
        hash = 37 * hash + occurredOn.hashCode();
        hash = 37 * hash + streamName.hashCode();
        hash = 37 * hash + streamVersion;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final StoredEventData other = (StoredEventData) obj;
        return eventId == other.eventId
                && eventType.equals(other.eventType)
                && Arrays.equals(eventBody, other.eventBody)
                && occurredOn.equals(other.occurredOn)
                && streamVersion == other.streamVersion
                && streamName.equals(other.streamName);
    }

    @Override
    public String toString() {
        return "StoredEventData{"
                + "eventId=" + eventId
                + ", eventType=" + eventType
                + ", eventBody=" + Arrays.toString(eventBody)
                + ", occurredOn=" + occurredOn
                + ", streamName=" + streamName
                + ", streamVersion=" + streamVersion
                + '}';
    }
}
