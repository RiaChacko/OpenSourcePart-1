package com.marcowillemart.eventstore.test;

import com.marcowillemart.eventstore.EventStoreGrpc;
import com.marcowillemart.eventstore.EventStoreGrpc.EventStoreBlockingStub;
import com.marcowillemart.eventstore.EventStoreGrpc.EventStoreStub;
import com.marcowillemart.grpc.client.GrpcChannelFactory;
import com.marcowillemart.grpc.client.GrpcClientProperties;
import com.marcowillemart.grpc.server.LocalGrpcServerPort;
import io.grpc.Channel;
import javax.net.ssl.SSLException;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@SuppressWarnings("ProtectedField")
public abstract class AbstractIntegrationTest {

    @LocalGrpcServerPort
    private int port;

    @Autowired
    private GrpcClientProperties clientProperties;

    @Autowired
    private GrpcChannelFactory channelFactory;

    protected EventStoreBlockingStub client;
    protected EventStoreStub asyncClient;

    @Before
    public void setUp() throws SSLException {
        clientProperties.getChannels().get("default").setPort(port);

        Channel channel = channelFactory.createChannel("default");

        client = EventStoreGrpc.newBlockingStub(channel);
        asyncClient = EventStoreGrpc.newStub(channel);
    }
}
