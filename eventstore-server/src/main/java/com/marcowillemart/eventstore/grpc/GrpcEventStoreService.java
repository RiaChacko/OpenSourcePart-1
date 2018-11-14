package com.marcowillemart.eventstore.grpc;

import com.google.protobuf.Empty;
import com.marcowillemart.eventstore.AllStoredEventsRequest;
import com.marcowillemart.eventstore.AllStoredEventsResponse;
import com.marcowillemart.eventstore.AppendToStreamRequest;
import com.marcowillemart.eventstore.AppendToStreamResponse;
import com.marcowillemart.eventstore.CountStoredEventsResponse;
import com.marcowillemart.eventstore.EventData;
import com.marcowillemart.eventstore.EventStoreGrpc.EventStoreImplBase;
import com.marcowillemart.eventstore.EventStreamExistsRequest;
import com.marcowillemart.eventstore.EventStreamExistsResponse;
import com.marcowillemart.eventstore.LastStoredEventIdResponse;
import com.marcowillemart.eventstore.LoadEventStreamRequest;
import com.marcowillemart.eventstore.LoadEventStreamResponse;
import com.marcowillemart.eventstore.StoredEventData;
import com.marcowillemart.eventstore.SubscribeToAllRequest;
import com.marcowillemart.eventstore.WrongExpectedVersion;
import com.marcowillemart.eventstore.application.EventApplicationService;
import com.marcowillemart.eventstore.application.EventQueryService;
import com.marcowillemart.eventstore.application.SubscriptionApplicationService;
import io.grpc.stub.StreamObserver;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GrpcEventStoreService extends EventStoreImplBase {

    /** AppendToStream success response. */
    private static final AppendToStreamResponse SUCCESS =
            AppendToStreamResponse.newBuilder()
                    .setSuccess(true)
                    .build();

    @Autowired
    private EventApplicationService eventApplicationService;

    @Autowired
    private EventQueryService eventQueryService;

    @Autowired
    private SubscriptionApplicationService subscriptionApplicationService;

    public GrpcEventStoreService() {
    }

    @Override
    public void countStoredEvents(
            Empty request,
            StreamObserver<CountStoredEventsResponse> responseObserver) {

        responseObserver.onNext(
                CountStoredEventsResponse.newBuilder()
                        .setCount(eventQueryService.countStoredEvents())
                        .build());

        responseObserver.onCompleted();
    }

    @Override
    public void lastStoredEventId(
            Empty request,
            StreamObserver<LastStoredEventIdResponse> responseObserver) {

        responseObserver.onNext(
                LastStoredEventIdResponse.newBuilder()
                        .setLastEventId(eventQueryService.lastStoredEventId())
                        .build());

        responseObserver.onCompleted();
    }

    @Override
    public void eventStreamExists(
            EventStreamExistsRequest request,
            StreamObserver<EventStreamExistsResponse> responseObserver) {

        responseObserver.onNext(
                EventStreamExistsResponse.newBuilder()
                        .setStreamExists(
                                eventQueryService.eventStreamExists(
                                        request.getStreamName()))
                        .build());

        responseObserver.onCompleted();
    }

    @Override
    public void allStoredEvents(
            AllStoredEventsRequest request,
            StreamObserver<AllStoredEventsResponse> responseObserver) {

        responseObserver.onNext(
                AllStoredEventsResponse.newBuilder()
                        .addAllEvents(
                                eventQueryService.allStoredEvents(
                                        request.getLowEventId(),
                                        request.getHighEventId()))
                        .build());

        responseObserver.onCompleted();
    }

    @Override
    public void loadEventStream(
            LoadEventStreamRequest request,
            StreamObserver<LoadEventStreamResponse> responseObserver) {

        List<StoredEventData> events =
                eventQueryService.loadEventStreamAfter(
                        request.getStreamName(),
                        request.getAfterStreamVersion());

        responseObserver.onNext(
                LoadEventStreamResponse.newBuilder()
                        .setStreamVersion(
                                request.getAfterStreamVersion() + events.size())
                        .addAllEvents(events)
                .build());

        responseObserver.onCompleted();
    }

    @Override
    public void append(
            EventData event,
            StreamObserver<StoredEventData> responseObserver) {

        responseObserver.onNext(eventApplicationService.append(event));
        responseObserver.onCompleted();
    }

    @Override
    public void appendToStream(
            AppendToStreamRequest request,
            StreamObserver<AppendToStreamResponse> responseObserver) {

            Optional<WrongExpectedVersion> wrongExpectedVersion =
                    eventApplicationService.appendToStream(
                            request.getStreamName(),
                            request.getExpectedVersion(),
                            request.getEventsList());

            if (wrongExpectedVersion.isPresent()) {
                responseObserver.onNext(
                        AppendToStreamResponse.newBuilder()
                                .setWrongExpectedVersion(
                                        wrongExpectedVersion.get())
                                .build());
            } else {
                responseObserver.onNext(SUCCESS);
            }

        responseObserver.onCompleted();
    }

    @Override
    public void subscribeToAll(
            SubscribeToAllRequest request,
            StreamObserver<StoredEventData> responseObserver) {

        subscriptionApplicationService.register(

                new GrpcSubscription(
                        request.getAfterEventId(),
                        responseObserver));
    }
}
