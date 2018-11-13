package com.marcowillemart.eventstore.domain.subscription;

import com.marcowillemart.eventstore.domain.StoredEventData;

public interface Subscription {

    void deliver(StoredEventData storedEvent);

    long afterEventId();

    long lastDeliveredEventId();
}
