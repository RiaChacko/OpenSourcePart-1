package com.marcowillemart.eventstore.domain;

import java.util.List;

/**
 * EventStoreEngine represents a mutable storage engine for events.
 *
 * It provides a mechanism to append as well as retrieve events from an
 * underlying storage like a database. An event storage engine can also be used
 * to store and fetch aggregate snapshots.
 *
 * @author mwi
 */
public interface EventStorageEngine {

    ////////////////////
    // READING EVENTS
    ////////////////////

    /**
     * @return the number of stored events in this.
     */
    long countStoredEvents();

    /**
     * @return the id of the last event stored in this if any, else returns 0.
     */
    long lastStoredEventId();

    /**
     * @return the stored events evts such that for all e in evts, e.eventId in
     *         [lowEventId..highEventId] and e is sorted by its id in ascending
     *         order.
     */
    List<StoredEventData> allStoredEventsBetween(
            long lowEventId,
            long highEventId);

    /**
     * @return the stored events evts such that for all e in evts,
     *         e.eventId > eventId and all e are sorted by their id in
     *         ascending order.
     */
    List<StoredEventData> allStoredEventsAfter(long eventId);

    /**
     * @requires streamName != null
     * @return true iff an event stream named streamName exists in this
     */
    boolean eventStreamExists(String streamName);

    /**
     * @requires streamName != null && version >= 0
     * @return the event stream named streamName in this starting from
     *         version + 1
     */
    List<StoredEventData> loadEventStreamAfter(String streamName, int version);

    ////////////////////
    // WRITING EVENTS
    ////////////////////

    /**
     * @requires event != null
     * @modifies this
     * @effects Stores the given event in a new stored event se and appends se
     *          to this.
     * @return se, i.e., the created stored event.
     */
    StoredEventData append(EventData event);

    /**
     * @requires streamName not null and not empty &&
     *           expectedVersion >= 0 &&
     *           events not null
     * @modifies this
     * @effects Appends the given events to the stream named streamName in this
     * @throws WrongExpectedVersionException if the version at which we
     *         currently expect the stream to be is different than
     *         expectedVersion
     */
    void appendToStream(
            String streamName,
            int expectedVersion,
            List<EventData> events) throws WrongExpectedVersionException;

    ////////////////////
    // NOTIFICATIONS
    ////////////////////

    /**
     * @requires eventNotifiable != null
     * @modifies this
     * @effects Registers the given event notifiable and removes the previously
     *          registered one, if any.
     *          The registered event notifiable will be notified when new stored
     *          events are ready to be dispatched.
     */
    void register(EventNotifiable eventNotifiable);
}
