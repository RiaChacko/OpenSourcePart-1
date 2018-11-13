-- *********************************************
-- * SQL PostgreSQL/H2
-- *--------------------------------------------
-- * Schema: Event Store
-- *********************************************


-- Tables Section
-- _____________

create table EVENT_STORE (
    EVENT_ID bigserial not null,
    EVENT_TYPE varchar(255) not null,
    EVENT_BODY bytea not null,
    OCCURRED_ON timestamp not null,
    STREAM_NAME varchar(255),
    STREAM_VERSION int,
    constraint ID_EVENT_STORE primary key (EVENT_ID),
    constraint SID_EVENT_STORE unique (STREAM_NAME, STREAM_VERSION)
);


-- Constraints Section
-- ___________________

alter table EVENT_STORE add constraint CHECK_DOM_EVENT_TYPE
    check(EVENT_TYPE <> '');

alter table EVENT_STORE add constraint CHECK_COEX_STREAM
    check((STREAM_NAME is not null and STREAM_VERSION is not null)
        or (STREAM_NAME is null and STREAM_VERSION is null));

alter table EVENT_STORE add constraint CHECK_DOM_STREAM_NAME
    check(STREAM_NAME <> '');

alter table EVENT_STORE add constraint CHECK_DOM_STREAM_VERSION
    check(STREAM_VERSION > 0);


-- Index Section
-- _____________

-- Indexes are created automatically for primary key and unique constraints.
-- Indexes are also created for foreign key constraints, if required.
-- For other columns, indexes need to be created manually.
