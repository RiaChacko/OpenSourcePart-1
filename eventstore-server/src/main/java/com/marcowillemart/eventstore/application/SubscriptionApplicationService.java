package com.marcowillemart.eventstore.application;

import com.marcowillemart.common.util.Assert;
import com.marcowillemart.eventstore.domain.subscription.Subscription;
import com.marcowillemart.eventstore.domain.subscription.SubscriptionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionApplicationService {

    private static final Logger LOG =
            LoggerFactory.getLogger(SubscriptionApplicationService.class);

    private final SubscriptionManager subscriptionManager;

    public SubscriptionApplicationService(
            SubscriptionManager subscriptionManager) {

        Assert.notNull(subscriptionManager);

        this.subscriptionManager = subscriptionManager;
    }

    public void register(Subscription subscription) {
        subscriptionManager.register(subscription);

        LOG.info("Subscription starting from event '{}' registered",
                subscription.afterEventId());
    }
}
