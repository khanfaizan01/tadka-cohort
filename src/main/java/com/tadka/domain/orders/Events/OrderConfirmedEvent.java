package com.tadka.domain.orders.Events;

import com.tadka.domain.common.IDomainEvent;
import java.time.LocalDateTime;
import java.util.UUID;

public final class OrderConfirmedEvent implements IDomainEvent {
    private final UUID orderId;
    private final UUID customerId;
    private final LocalDateTime occurredAt = LocalDateTime.now();

    public OrderConfirmedEvent(UUID orderId, UUID customerId) {
        this.orderId = orderId;
        this.customerId = customerId;
    }

    public UUID getOrderId() { return orderId; }
    public UUID getCustomerId() { return customerId; }
    @Override public LocalDateTime getOccurredAt() { return occurredAt; }
}
