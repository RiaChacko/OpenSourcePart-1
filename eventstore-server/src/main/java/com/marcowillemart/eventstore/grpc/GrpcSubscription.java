package com.marcowillemart.eventstore.grpc;

import com.marcowillemart.common.util.Assert;
import com.marcowillemart.eventstore.domain.StoredEventData;
import com.marcowillemart.eventstore.domain.subscription.AbstractSubscription;
import io.grpc.stub.StreamObserver;

final class GrpcSubscription extends AbstractSubscription {

    private final StreamObserver<com.marcowillemart.eventstore.StoredEventData>
            observer;

    GrpcSubscription(
            long afterEventId,
            StreamObserver<com.marcowillemart.eventstore.StoredEventData>
                    observer) {

        super(afterEventId);

        Assert.notNull(observer);

        this.observer = observer;
    }

    @Override
    protected void doDeliver(StoredEventData storedEvent) {
        observer.onNext(ProtoTranslator.toProto(storedEvent));
    }
}
