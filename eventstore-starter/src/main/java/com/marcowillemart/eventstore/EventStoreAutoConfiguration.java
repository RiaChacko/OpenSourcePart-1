package com.marcowillemart.eventstore;

import com.marcowillemart.common.event.EventStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration for EventStore.
 *
 * @author Marco Willemart
 */
@Configuration
@EnableConfigurationProperties(EventStoreProperties.class)
//@AutoConfigureBefore(GrpcClientAutoConfiguration.class)
@ConditionalOnClass(EventStore.class)
@ConditionalOnProperty(
        prefix = "eventstore.embedded",
        name = "enabled",
        havingValue = "false")
public class EventStoreAutoConfiguration {

    public EventStoreAutoConfiguration() {
//        Assert.notNull(properties);
//        Assert.notNull(grpcProperties);
//
//        GrpcClientProperties.ChannelConfiguration channel =
//                grpcProperties
//                        .getChannels()
//                        .get(GrpcClientProperties.DEFAULT_CHANNEL);
//
//        channel.setHost(properties.getHost());
//        channel.setPort(properties.getPort());
    }

//    @Bean
//    @ConditionalOnMissingBean(EventStore.class)
//    public EventStore inmemoryEventStore() {
//        return new InMemoryEventStore();
//    }
//
//    @Bean
//    @ConditionalOnClass(GrpcEventStore.class)
//    public EventStore grpcEventStore(GrpcChannelFactory channelFactory) {
//        return new GrpcEventStore(channelFactory);
//    }
}
