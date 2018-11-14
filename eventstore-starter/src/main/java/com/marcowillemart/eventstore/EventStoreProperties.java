 package com.marcowillemart.eventstore;

import com.marcowillemart.common.util.Assert;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for EventStore.
 *
 * @specfield host : String       // Host of the event store.
 * @specfield port : int          // Port of the event store.
 *
 * @invariant host not empty
 * @invariant port > 0
 *
 * @author Marco Willemart
 */
@ConfigurationProperties("eventstore")
public class EventStoreProperties {

    private String host;
    private int port;

    private void checkRep() {
        Assert.notEmpty(host);
        Assert.isTrue(port > 0);
    }

    /**
     * @effects Makes this be a new EventStoreProperties p with
     *          p.host = localhost,
     *          p.port = 2525
     */
    public EventStoreProperties() {
        this.host = "localhost";
        this.port = 2525;

        checkRep();
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public void setHost(String host) {
        this.host = host;

        checkRep();
    }

    public void setPort(int port) {
        this.port = port;

        checkRep();
    }
}
