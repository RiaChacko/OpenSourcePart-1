package com.marcowillemart.eventstore.infrastructure.persistence.schema;

import com.marcowillemart.common.util.Assert;

public final class Tables {

    public static final String EVENT_STORE = "EVENT_STORE";

    private Tables() {
        Assert.shouldNeverGetHere();
    }
}
