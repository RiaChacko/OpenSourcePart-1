package com.marcowillemart.eventstore.infrastructure.persistence.query;

import com.marcowillemart.eventstore.infrastructure.persistence.schema.Columns;
import com.marcowillemart.eventstore.infrastructure.persistence.schema.Tables;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public final class LoadEventStreamAfter extends AbstractQuery {

    private static final String SQL = String.format(
            "select * from %s where %s = ? and %s > ? order by %s asc",
            Tables.EVENT_STORE,
            Columns.STREAM_NAME,
            Columns.STREAM_VERSION,
            Columns.STREAM_VERSION);

    private String streamName;
    private int version;

    private LoadEventStreamAfter() {
        super(SQL);
    }

    public LoadEventStreamAfter streamName(String streanName) {
        this.streamName = streanName;
        return this;
    }

    public LoadEventStreamAfter version(int version) {
        this.version = version;
        return this;
    }

    @Override
    protected void build(PreparedStatement ps) throws SQLException {
        ps.setString(1, streamName);
        ps.setInt(2, version);
    }

    public static LoadEventStreamAfter newQuery() {
        return new LoadEventStreamAfter();
    }
}
