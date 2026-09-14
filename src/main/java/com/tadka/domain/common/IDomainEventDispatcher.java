package com.tadka.domain.common;

import java.util.List;

public interface IDomainEventDispatcher {
    void dispatch(List<IDomainEvent> events);
}
