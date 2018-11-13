package com.marcowillemart.eventstore.domain.subscription;

import com.marcowillemart.common.util.Assert;
import com.marcowillemart.eventstore.domain.StoredEventData;

public abstract class AbstractSubscription implements Subscription {

    private final long afterEventId;
    private long lastDeliveredEventId;

    private void checkRep() {
        Assert.isTrue(afterEventId >= 0L);
        Assert.isTrue(lastDeliveredEventId >= afterEventId);
    }

    public AbstractSubscription(long afterEventId) {
        this.afterEventId = afterEventId;
        this.lastDeliveredEventId = afterEventId;

        checkRep();
    }

    @Override
    public final void deliver(StoredEventData storedEvent) {
        Assert.notNull(storedEvent);
        Assert.isTrue(afterEventId < storedEvent.eventId());

        doDeliver(storedEvent);

        lastDeliveredEventId = storedEvent.eventId();

        checkRep();
    }

    @Override
    public final long afterEventId() {
        return afterEventId;
    }

    @Override
    public final long lastDeliveredEventId() {
        return lastDeliveredEventId;
    }

    protected abstract void doDeliver(StoredEventData storedEvent);
}
