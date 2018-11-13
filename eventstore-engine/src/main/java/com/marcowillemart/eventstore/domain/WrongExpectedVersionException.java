package com.marcowillemart.eventstore.domain;

import com.marcowillemart.common.util.Assert;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class WrongExpectedVersionException extends Exception {

    private final String streamName;
    private final int expectedVersion;
    private final int actualVersion;
    private final List<StoredEventData> actualEvents;

    public WrongExpectedVersionException(
            String streamName,
            int expectedVersion,
            int actualVersion,
            List<StoredEventData> actualEvents,
            Throwable cause) {

        super(String.format(
                "Expected v%d but found v%d in stream '%s'",
                expectedVersion,
                actualVersion,
                streamName),
                cause);

        Assert.notEmpty(streamName);
        Assert.isTrue(expectedVersion >= 0);
        Assert.isTrue(actualVersion > 0);
        Assert.notNull(actualEvents);
        Assert.equiv(expectedVersion < actualVersion, !actualEvents.isEmpty());
        Assert.equiv(expectedVersion > actualVersion, actualEvents.isEmpty());

        this.streamName = streamName;
        this.expectedVersion = expectedVersion;
        this.actualVersion = actualVersion;
        this.actualEvents = new ArrayList<>(actualEvents);
    }

    public String streamName() {
        return streamName;
    }

    public int expectedVersion() {
        return expectedVersion;
    }

    public int actualVersion() {
        return actualVersion;
    }

    public List<StoredEventData> actualEvents() {
        return Collections.unmodifiableList(actualEvents);
    }
}
