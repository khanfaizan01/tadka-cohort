package com.tadka.domain.orders.Events;

import com.tadka.domain.common.IDomainEvent;
import java.time.LocalDateTime;
import java.util.UUID;

public final class OrderPlacedEvent implements IDomainEvent {
    private final UUID orderId;
    private final UUID customerId;
    private final UUID restaurantId;
    private final LocalDateTime occurredAt = LocalDateTime.now();

    public OrderPlacedEvent(UUID orderId, UUID customerId, UUID restaurantId) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.restaurantId = restaurantId;
    }

    public UUID getOrderId() { return orderId; }
    public UUID getCustomerId() { return customerId; }
    public UUID getRestaurantId() { return restaurantId; }
    @Override public LocalDateTime getOccurredAt() { return occurredAt; }
}
