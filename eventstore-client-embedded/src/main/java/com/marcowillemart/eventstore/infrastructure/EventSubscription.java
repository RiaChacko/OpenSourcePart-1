package com.marcowillemart.eventstore.infrastructure;

import com.google.protobuf.Message;
import com.marcowillemart.common.util.Assert;
import com.marcowillemart.eventstore.domain.StoredEventData;
import com.marcowillemart.eventstore.domain.subscription.AbstractSubscription;
import java.util.function.Consumer;

final class EventSubscription extends AbstractSubscription {

    private final Consumer<Message> subscriber;

    EventSubscription(
            long afterEventId,
            Consumer<Message> subscriber) {

        super(afterEventId);

        Assert.notNull(subscriber);

        this.subscriber = subscriber;
    }

    @Override
    protected void doDeliver(StoredEventData storedEvent) {
        subscriber.accept(EventTranslator.toMessage(storedEvent));
    }
}
