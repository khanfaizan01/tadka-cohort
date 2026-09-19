package com.tadka.infrastructure.realtime;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * No-op tracking bus — used when Redis is not configured. Live tracking returns 503.
 */
public class NullOrderTrackingBus implements IOrderTrackingBus {

    @Override
    public boolean isEnabled() { return false; }

    @Override
    public CompletableFuture<Void> publishAsync(OrderTrackingEvent trackingEvent) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> subscribe(UUID orderId, SequencedTrackingEvent event) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public List<SequencedTrackingEvent> getEventsSince(UUID orderId, long sinceSeq) {
        return List.of();
    }
}
