package com.marcowillemart.eventstore;

import com.marcowillemart.grpc.client.GrpcChannelFactory;
import com.marcowillemart.grpc.client.GrpcClientAutoConfiguration;
import com.marcowillemart.grpc.client.GrpcClientProperties;
import static org.assertj.core.api.Assertions.*;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

@Ignore
public class EventStoreAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withConfiguration(
                            AutoConfigurations.of(
                                    EventStoreAutoConfiguration.class,
                                    GrpcClientAutoConfiguration.class));

    @Test
    public void testDefaultAutoConfiguration() {
//        contextRunner.run(context -> {
//            assertThat(context).hasSingleBean(EventStoreProperties.class);
//
//            EventStoreProperties properties =
//                    context.getBean(EventStoreProperties.class);
//
//            assertThat(properties.getHost()).isEqualTo("localhost");
//            assertThat(properties.getPort()).isEqualTo(2525);
//            assertThat(properties.getEmbedded().isEnabled()).isFalse();
//            assertThat(properties.getEmbedded().getDriverClassName()).isEmpty();
//            assertThat(properties.getEmbedded().getUrl()).isEmpty();
//            assertThat(properties.getEmbedded().getUsername()).isEmpty();
//            assertThat(properties.getEmbedded().getPassword()).isEmpty();
//        });
    }

    @Test
    public void testGrpcClientProperties() {
        contextRunner
                .withPropertyValues(
                        "eventstore.host=cloud.marcowillemart.com",
                        "eventstore.port=2018")
                .run(context -> {
                    assertThat(context)
                            .hasSingleBean(GrpcClientProperties.class);

                    assertThat(
                            context.getBean(GrpcClientProperties.class)
                                    .getChannels()
                                    .get("default")
                                    .getHost())
                            .isEqualTo("cloud.marcowillemart.com");

                    assertThat(
                            context.getBean(GrpcClientProperties.class)
                                    .getChannels()
                                    .get("default")
                                    .getPort())
                            .isEqualTo(2018);
                });
    }

    @Test
    public void testGrpcClientAutoConfiguration() {
        contextRunner
                .run(context -> {
                    assertThat(context)
                            .hasSingleBean(GrpcClientProperties.class);

                    assertThat(
                            context.getBean(GrpcClientProperties.class)
                                    .getChannels()
                                    .get("default")
                                    .getPort())
                            .isEqualTo(2525);

                    assertThat(context).hasSingleBean(GrpcChannelFactory.class);

                    assertThat(
                            context.getBean(GrpcChannelFactory.class)
                                    .createChannel("default"))
                            .isNotNull();
                });
    }
}
