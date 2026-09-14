package com.tadka.domain.common;

import java.time.LocalDateTime;

public interface IDomainEvent {
    LocalDateTime getOccurredAt();
}
