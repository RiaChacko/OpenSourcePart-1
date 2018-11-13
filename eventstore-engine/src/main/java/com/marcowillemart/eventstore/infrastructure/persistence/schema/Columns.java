package com.marcowillemart.eventstore.infrastructure.persistence.schema;

import com.marcowillemart.common.util.Assert;
import com.marcowillemart.common.util.Strings;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public final class Columns {

    public static final String EVENT_ID = "EVENT_ID";
    public static final String EVENT_TYPE = "EVENT_TYPE";
    public static final String EVENT_BODY = "EVENT_BODY";
    public static final String OCCURRED_ON = "OCCURRED_ON";
    public static final String STREAM_NAME = "STREAM_NAME";
    public static final String STREAM_VERSION = "STREAM_VERSION";

    private Columns() {
        Assert.shouldNeverGetHere();
    }

    public static long eventId(ResultSet rs) throws SQLException {
        return rs.getLong(EVENT_ID);
    }

    public static String eventType(ResultSet rs) throws SQLException {
        return rs.getString(EVENT_TYPE);
    }

    public static byte[] eventBody(ResultSet rs) throws SQLException {
        return rs.getBytes(EVENT_BODY);
    }

    public static LocalDateTime occurredOn(ResultSet rs) throws SQLException {
        return rs.getTimestamp(OCCURRED_ON).toLocalDateTime();
    }

    public static String streamName(ResultSet rs) throws SQLException {
        String streamName = rs.getString(STREAM_NAME);

        if (streamName == null) {
            return Strings.EMPTY;
        }

        return streamName;
    }

    public static int streamVersion(ResultSet rs) throws SQLException {
        return rs.getInt(STREAM_VERSION);
    }
}
