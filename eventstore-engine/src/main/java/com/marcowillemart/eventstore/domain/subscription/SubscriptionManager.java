package com.marcowillemart.eventstore.domain.subscription;

import com.marcowillemart.common.concurrent.GuardedBy;
import com.marcowillemart.common.concurrent.ThreadSafe;
import com.marcowillemart.common.util.Assert;
import com.marcowillemart.eventstore.domain.EventStorageEngine;
import java.util.LinkedHashSet;
import java.util.Set;

@ThreadSafe
public class SubscriptionManager {

    private final EventStorageEngine eventStorageEngine;

    @GuardedBy("this") private final Set<Subscription> subscriptions;

    public SubscriptionManager(EventStorageEngine eventStorageEngine) {
        Assert.notNull(eventStorageEngine);

        this.eventStorageEngine = eventStorageEngine;

        this.subscriptions = new LinkedHashSet<>();

        this.eventStorageEngine.register(this::deliverEvents);
    }

    public int subscriptionCount() {
        synchronized (this) {
            return subscriptions.size();
        }
    }

    public void register(Subscription subscription) {
        Assert.notNull(subscription);

        // TODO: consider pagination if there are lots of events

        // We don't want to synchronize this as it may take some time.

        eventStorageEngine.allStoredEventsAfter(subscription.afterEventId())
                .forEach(subscription::deliver);

        synchronized (this) {
            // Ensures the events appended after the call above are delivered.
            deliverEventsTo(subscription);

            subscriptions.add(subscription);
        }
    }

    ////////////////////
    // HELPER METHODS
    ////////////////////

    private void deliverEvents() {
        synchronized (this) {
            subscriptions.forEach(this::deliverEventsTo);
        }
    }

    private void deliverEventsTo(Subscription subscription) {
        // We could make only one call to fetch the last events but due to the
        // asynchronous nature of this code it seems risky. This way we ensure
        // that we don't miss any event. Also there shouldn't be a huge number
        // of subscriptions and even so queries such as this one are usually
        // quite fast.

        eventStorageEngine.allStoredEventsAfter(
                subscription.lastDeliveredEventId())
                .forEach(subscription::deliver);
    }
}
