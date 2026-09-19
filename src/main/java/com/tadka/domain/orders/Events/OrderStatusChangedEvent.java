package com.tadka.domain.orders.Events;

import com.tadka.domain.common.IDomainEvent;
import com.tadka.domain.orders.OrderStatus;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Raised on every successful state transition. Drives live tracking (ADR-020).
 */
public final class OrderStatusChangedEvent implements IDomainEvent {
    private final UUID orderId;
    private final OrderStatus status;
    private final LocalDateTime occurredAt = LocalDateTime.now();

    public OrderStatusChangedEvent(UUID orderId, OrderStatus status) {
        this.orderId = orderId;
        this.status = status;
    }

    public UUID getOrderId() { return orderId; }
    public OrderStatus getStatus() { return status; }
    @Override public LocalDateTime getOccurredAt() { return occurredAt; }
}
