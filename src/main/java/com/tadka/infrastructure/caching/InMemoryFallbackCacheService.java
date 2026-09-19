package com.tadka.infrastructure.caching;

import com.fasterxml.jackson.core.type.TypeReference;
import java.time.Duration;
import java.util.function.Supplier;

/**
 * In-process fallback cache — used when Cache:Mode=InMemory is set to demonstrate
 * why an in-process cache under multi-instance load is a trap.
 */
public class InMemoryFallbackCacheService implements ICacheService {

    @Override
    public <T> T getOrSet(String key, TypeReference<T> typeRef, Supplier<T> factory, Duration ttl) {
        return factory.get();
    }

    @Override
    public void invalidate(String key) {
        // no-op
    }
}
