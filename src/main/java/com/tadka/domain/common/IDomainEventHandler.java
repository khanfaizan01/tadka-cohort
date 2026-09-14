package com.tadka.domain.common;

public interface IDomainEventHandler<T extends IDomainEvent> {
    void handle(T event);
}
