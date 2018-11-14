package com.marcowillemart.eventstore.embedded;

import com.marcowillemart.common.event.EventStore;
import com.marcowillemart.eventstore.infrastructure.JdbcEventStore;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

public class EmbeddedEventStoreAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withConfiguration(
                            AutoConfigurations.of(
                                    EmbeddedEventStoreAutoConfiguration.class));

    @Test
    public void testEmbeddedAutoConfiguration() {
        contextRunner
                .withPropertyValues(
                        "eventstore.embedded.enabled=true",
                        "eventstore.embedded.driverClassName=org.h2.Driver",
                        "eventstore.embedded.url=jdbc:h2:mem:eventstore",
                        "eventstore.embedded.username=testUser",
                        "eventstore.embedded.password=testPwd")
                .run(context -> {
                    assertThat(context)
                            .hasSingleBean(EmbeddedEventStoreProperties.class);

                    EmbeddedEventStoreProperties properties =
                            context.getBean(EmbeddedEventStoreProperties.class);

                    assertThat(properties.isEnabled()).isTrue();
                    assertThat(properties.getDriverClassName())
                            .isEqualTo("org.h2.Driver");
                    assertThat(properties.getUrl())
                            .isEqualTo("jdbc:h2:mem:eventstore");
                    assertThat(properties.getUsername()).isEqualTo("testUser");
                    assertThat(properties.getPassword()).isEqualTo("testPwd");

                    assertThat(context).hasSingleBean(EventStore.class);
                    assertThat(context).hasSingleBean(JdbcEventStore.class);
        });
    }
}
