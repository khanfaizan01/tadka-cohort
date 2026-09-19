package com.tadka.domain.orders.Events.Handlers;

import com.tadka.domain.common.IDomainEventHandler;
import com.tadka.domain.orders.Events.OrderStatusChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OrderStatusChangedTrackingHandler implements IDomainEventHandler<OrderStatusChangedEvent> {

    private static final Logger log = LoggerFactory.getLogger(OrderStatusChangedTrackingHandler.class);

    @Value("${Demo.NotificationDelayMs:0}")
    private int notificationDelayMs;

    @Override
    public void handle(OrderStatusChangedEvent event) {
        if (notificationDelayMs > 0) {
            try { Thread.sleep(notificationDelayMs); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
        log.info("Order {} status changed to {} — published to live tracking", event.getOrderId(), event.getStatus());
    }
}
