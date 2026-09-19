package com.tadka.service;

import com.tadka.domain.common.IDomainEvent;
import com.tadka.domain.common.IDomainEventHandler;
import com.tadka.domain.common.IDomainEventDispatcher;
import com.tadka.domain.orders.Events.OrderConfirmedEvent;
import com.tadka.domain.orders.Events.OrderPlacedEvent;
import com.tadka.domain.orders.Events.OrderStatusChangedEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.util.List;

@Service
public class DomainEventDispatcher implements IDomainEventDispatcher {

    private final List<IDomainEventHandler<?>> handlers;

    public DomainEventDispatcher(List<IDomainEventHandler<?>> handlers) {
        this.handlers = handlers;
    }

    @Override
    public void dispatch(List<IDomainEvent> events) {
        if (events.isEmpty()) return;
        // Dispatch domain events AFTER the current transaction commits (ADR-013).
        // If the transaction rolls back, the events are discarded.
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                for (IDomainEvent event : events) {
                    for (IDomainEventHandler<?> handler : handlers) {
                        dispatchEvent(handler, event);
                    }
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void dispatchEvent(IDomainEventHandler<?> handler, IDomainEvent event) {
        if (event instanceof OrderPlacedEvent e) {
            ((IDomainEventHandler<OrderPlacedEvent>) handler).handle(e);
        } else if (event instanceof OrderConfirmedEvent e) {
            ((IDomainEventHandler<OrderConfirmedEvent>) handler).handle(e);
        } else if (event instanceof OrderStatusChangedEvent e) {
            ((IDomainEventHandler<OrderStatusChangedEvent>) handler).handle(e);
        }
        // Add more event types here as they grow
    }
}
