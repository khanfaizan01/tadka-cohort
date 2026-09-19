package com.tadka.infrastructure.realtime;

import com.tadka.domain.orders.OrderStatus;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A tracking event published to the Redis backplane whenever an order status changes (ADR-020).
 */
public record OrderTrackingEvent(UUID orderId, String status, String message, LocalDateTime occurredAt) {
    public OrderTrackingEvent(UUID orderId, String status, String message, LocalDateTime occurredAt) {
        this.orderId = orderId;
        this.status = status;
        this.message = message;
        this.occurredAt = occurredAt;
    }
}
