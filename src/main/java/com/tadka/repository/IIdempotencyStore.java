package com.tadka.repository;

import java.util.UUID;

public interface IIdempotencyStore {
    UUID findOrderId(String key);
    void record(String key, UUID orderId);
}
