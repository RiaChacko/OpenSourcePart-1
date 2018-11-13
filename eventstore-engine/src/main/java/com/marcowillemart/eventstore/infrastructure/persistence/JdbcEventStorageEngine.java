package com.marcowillemart.eventstore.infrastructure.persistence;

import com.marcowillemart.common.util.Assert;
import com.marcowillemart.eventstore.domain.EventData;
import com.marcowillemart.eventstore.domain.EventNotifiable;
import com.marcowillemart.eventstore.domain.EventStorageEngine;
import com.marcowillemart.eventstore.domain.StoredEventData;
import com.marcowillemart.eventstore.domain.WrongExpectedVersionException;
import com.marcowillemart.eventstore.infrastructure.persistence.query.AllStoredEventsAfter;
import com.marcowillemart.eventstore.infrastructure.persistence.query.AllStoredEventsBetween;
import com.marcowillemart.eventstore.infrastructure.persistence.query.Append;
import com.marcowillemart.eventstore.infrastructure.persistence.query.AppendToStream;
import com.marcowillemart.eventstore.infrastructure.persistence.query.CountStoredEvents;
import com.marcowillemart.eventstore.infrastructure.persistence.query.EventStreamExists;
import com.marcowillemart.eventstore.infrastructure.persistence.query.LastStoredEventId;
import com.marcowillemart.eventstore.infrastructure.persistence.query.LoadEventStreamAfter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

public class JdbcEventStorageEngine implements EventStorageEngine {

    /** Initial version of an event stream, i.e., indicates an empty stream. */
    private static final int INITIAL_VERSION = 0;

    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactionTemplate;
    private final TransactionTemplate readOnlyTransactionTemplate;
    private final boolean postgres;
    private Optional<EventNotifiable> eventNotifiable;

    public JdbcEventStorageEngine(
            DataSource dataSource,
            PlatformTransactionManager transactionManager) {

        Assert.notNull(dataSource);
        Assert.notNull(transactionManager);

        this.jdbcTemplate = new JdbcTemplate(dataSource);

        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(
                TransactionDefinition.PROPAGATION_NESTED);

        this.readOnlyTransactionTemplate =
                new TransactionTemplate(transactionManager);
        this.readOnlyTransactionTemplate.setReadOnly(true);

        this.postgres = DatabaseTypes.isPostgres(dataSource);

        this.eventNotifiable = Optional.empty();
    }

    @Override
    public long countStoredEvents() {
        return CountStoredEvents.newQuery()
                .queryForObject(
                        readOnlyTransactionTemplate,
                        jdbcTemplate,
                        Long.class);
    }

    @Override
    public long lastStoredEventId() {
        Optional<Long> lastEventId =
                Optional.ofNullable(
                        LastStoredEventId.newQuery()
                                .queryForObject(
                                        readOnlyTransactionTemplate,
                                        jdbcTemplate,
                                        Long.class));

        return lastEventId.orElse(0L);
    }

    @Override
    public List<StoredEventData> allStoredEventsBetween(
            long lowEventId,
            long highEventId) {

        return AllStoredEventsBetween.newQuery()
                .lowEventId(lowEventId)
                .highEventId(highEventId)
                .query(
                        readOnlyTransactionTemplate,
                        jdbcTemplate,
                        ResultSetTranslator::storedEventFrom);
    }

    @Override
    public List<StoredEventData> allStoredEventsAfter(long eventId) {
        return AllStoredEventsAfter.newQuery()
                .eventId(eventId)
                .query(
                        readOnlyTransactionTemplate,
                        jdbcTemplate,
                        ResultSetTranslator::storedEventFrom);
    }

    @Override
    public boolean eventStreamExists(String streamName) {
        Assert.notNull(streamName);

        return EventStreamExists.newQuery()
                .streamName(streamName)
                .exists(readOnlyTransactionTemplate, jdbcTemplate);
    }

    @Override
    public List<StoredEventData> loadEventStreamAfter(
            String streamName,
            int version) {

        Assert.notNull(streamName);
        Assert.isTrue(version >= INITIAL_VERSION);

        return  LoadEventStreamAfter.newQuery()
                .streamName(streamName)
                .version(version)
                .query(
                        readOnlyTransactionTemplate,
                        jdbcTemplate,
                        ResultSetTranslator::storedEventFrom);
    }

    @Override
    public StoredEventData append(EventData event) {
        Assert.notNull(event);

        LocalDateTime occurredOn = LocalDateTime.now();

        Number generatedKey =
                Append.newQuery(postgres)
                        .eventType(event.type())
                        .eventBody(event.body())
                        .occurredOn(occurredOn)
                        .update(transactionTemplate, jdbcTemplate);

        notifyDispatchableEvents();

        return new StoredEventData(
                generatedKey.longValue(),
                event.type(),
                event.body(),
                occurredOn);
    }

    @Override
    public void appendToStream(
            String streamName,
            int expectedVersion,
            List<EventData> events) throws WrongExpectedVersionException {

        Assert.notEmpty(streamName);
        Assert.isTrue(expectedVersion >= INITIAL_VERSION);
        Assert.notNull(events);

        if (events.isEmpty()) {
            return;
        }

        AppendToStream query =
                AppendToStream.newQuery()
                        .streamName(streamName)
                        .expectedVersion(expectedVersion)
                        .events(events)
                        .occurredOn(LocalDateTime.now());
        try {
            query.batchUpdate(transactionTemplate, jdbcTemplate);
        } catch (DataIntegrityViolationException ex) {
            throw wrongExpectedVersionExceptionFrom(
                    streamName,
                    expectedVersion,
                    ex);
        }

        notifyDispatchableEvents();
    }

    @Override
    public void register(EventNotifiable eventNotifiable) {
        Assert.notNull(eventNotifiable);

        this.eventNotifiable = Optional.of(eventNotifiable);
    }

    ////////////////////
    // HELPER METHODS
    ////////////////////

    private WrongExpectedVersionException wrongExpectedVersionExceptionFrom(
            String streamName,
            int expectedVersion,
            Throwable cause) {

        // TODO: Handle the case where actualVersion < expectedVersion

        List<StoredEventData> actualEvents =
                loadEventStreamAfter(streamName, expectedVersion);

        return new WrongExpectedVersionException(
                streamName,
                expectedVersion,
                expectedVersion + actualEvents.size(),
                actualEvents,
                cause);
    }

    private void notifyDispatchableEvents() {
        eventNotifiable.ifPresent(EventNotifiable::notifyDispatchableEvents);
    }
}
