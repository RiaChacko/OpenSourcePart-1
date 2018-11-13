package com.marcowillemart.eventstore.domain;

import com.marcowillemart.common.util.Assert;

public final class EventData {

    private final String type;
    private final byte[] body;

    public EventData(String type, byte[] body) {
        Assert.notNull(type);
        Assert.isFalse(type.isEmpty());
        Assert.notNull(body);

        this.type = type;
        this.body = body.clone();
    }

    public String type() {
        return type;
    }

    public byte[] body() {
        return body.clone();
    }
}
