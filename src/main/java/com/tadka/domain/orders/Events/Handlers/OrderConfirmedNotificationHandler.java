package com.tadka.domain.orders.Events.Handlers;

import com.tadka.domain.common.IDomainEventHandler;
import com.tadka.domain.orders.Events.OrderConfirmedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OrderConfirmedNotificationHandler implements IDomainEventHandler<OrderConfirmedEvent> {

    private static final Logger log = LoggerFactory.getLogger(OrderConfirmedNotificationHandler.class);

    @Value("${Demo.NotificationDelayMs:0}")
    private int notificationDelayMs;

    @Override
    public void handle(OrderConfirmedEvent event) {
        if (notificationDelayMs > 0) {
            try { Thread.sleep(notificationDelayMs); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
        log.info("Order {} confirmed — notification sent to customer {}", event.getOrderId(), event.getCustomerId());
    }
}
