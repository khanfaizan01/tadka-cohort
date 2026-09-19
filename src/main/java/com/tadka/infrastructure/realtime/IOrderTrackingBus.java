package com.tadka.infrastructure.realtime;

import java.util.List;
import java.util.UUID;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Live-tracking backplane contract (ADR-020). Publishing and subscribing both go through
 * Redis channel "order:{id}", so the publishing instance and the connection-holding
 * instance need not be the same box.
 */
public interface IOrderTrackingBus {

    boolean isEnabled();

    CompletableFuture<Void> publishAsync(OrderTrackingEvent trackingEvent);

    CompletableFuture<Void> subscribe(UUID orderId, SequencedTrackingEvent event);

    List<SequencedTrackingEvent> getEventsSince(UUID orderId, long sinceSeq);
}
