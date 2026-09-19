package com.tadka.infrastructure.caching;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * No-op cache — used when Redis is not configured. Every call passes through to the DB.
 */
public class NullCacheService implements ICacheService {

    @Override
    public <T> T getOrSet(String key, TypeReference<T> typeRef, java.util.function.Supplier<T> factory, java.time.Duration ttl) {
        return factory.get();
    }

    @Override
    public void invalidate(String key) {
        // no-op
    }
}
