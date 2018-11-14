package com.marcowillemart.eventstore.test;

import com.google.protobuf.Message;
import static com.marcowillemart.common.test.Tests.*;
import com.marcowillemart.common.util.Resources;
import com.marcowillemart.eventstore.EventData;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.springframework.core.io.ClassPathResource;

public final class Tests {

    private Tests() {
        throw new AssertionError();
    }

    ////////////////////
    // CREATION METHODS
    ////////////////////

    public static EventData createAnonymousEventData() {
        Message message = createAnonymousMessage();

        return EventData.newBuilder()
                .setType(message.getClass().getName())
                .setBody(message.toByteString())
                .build();
    }

    public static List<EventData> createAnonymousEvents(int nbOfEvents) {
        List<EventData> events = new ArrayList<>(nbOfEvents);

        for (int i = 0; i < nbOfEvents; i++) {
            events.add(createAnonymousEventData());
        }

        return events;
    }

    ////////////////////
    // UTILITY METHODS
    ////////////////////

    public static File fileFromClasspath(String path) {
        return Resources.toFile(new ClassPathResource(path));
    }

    ////////////////////
    // CUSTOM ASSERTIONS
    ////////////////////

    ////////////////////
    // HELPER METHODS
    ////////////////////
}
