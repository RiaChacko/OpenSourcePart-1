package com.marcowillemart.eventstore.domain;

/**
 * EventNotifiable is an interface for notifying when events are readed to be
 * dispatched.
 *
 * @author mwi
 */
public interface EventNotifiable {

    /**
     * @modifies Anything
     * @effects Anything
     */
    void notifyDispatchableEvents();
}
