package com.tadka.infrastructure.caching;

import com.fasterxml.jackson.core.type.TypeReference;
import java.time.Duration;

/**
 * Cache-aside contract (ADR-018) with single-flight stampede protection (ADR-019).
 * Redis is a performance dependency, not a correctness one — any failure degrades to the DB.
 */
public interface ICacheService {

    <T> T getOrSet(String key, TypeReference<T> typeRef, java.util.function.Supplier<T> factory, Duration ttl);

    void invalidate(String key);
}
