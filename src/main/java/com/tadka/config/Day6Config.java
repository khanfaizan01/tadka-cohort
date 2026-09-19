package com.tadka.config;

import com.tadka.infrastructure.caching.ICacheService;
import com.tadka.infrastructure.caching.InMemoryFallbackCacheService;
import com.tadka.infrastructure.caching.NullCacheService;
import com.tadka.infrastructure.caching.RedisCacheService;
import com.tadka.infrastructure.ratelimiting.IRateLimiter;
import com.tadka.infrastructure.ratelimiting.NullRateLimiter;
import com.tadka.infrastructure.ratelimiting.RedisFixedWindowRateLimiter;
import com.tadka.infrastructure.ratelimiting.RedisSlidingWindowRateLimiter;
import com.tadka.infrastructure.realtime.IOrderTrackingBus;
import com.tadka.infrastructure.realtime.NullOrderTrackingBus;
import com.tadka.infrastructure.realtime.RedisOrderTrackingBus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Day 6 configuration: wires Redis-backed infrastructure beans
 * with graceful fallbacks when Redis is not configured.
 */
@Configuration
@EnableAsync
public class Day6Config {

    @Bean
    @Primary
    public ICacheService cacheService(RedisConnectionFactory redisConnectionFactory,
                                       StringRedisTemplate redisTemplate,
                                       @Value("${cache.mode:}") String cacheMode) {
        if ("InMemory".equalsIgnoreCase(cacheMode)) {
            return new InMemoryFallbackCacheService();
        }
        if (redisConnectionFactory != null) {
            return new RedisCacheService(redisTemplate);
        }
        return new NullCacheService();
    }

    @Bean
    @Primary
    public IRateLimiter rateLimiter(RedisConnectionFactory redisConnectionFactory,
                                      StringRedisTemplate redisTemplate,
                                      @Value("${rate-limit.algorithm:FixedWindow}") String algorithm,
                                      @Value("${rate-limit.per-minute:120}") int perMinute,
                                      @Value("${rate-limit.window-seconds:60}") int windowSeconds) {
        if (redisConnectionFactory == null) {
            return new NullRateLimiter();
        }
        long windowMillis = windowSeconds * 1000L;
        if ("SlidingWindow".equalsIgnoreCase(algorithm)) {
            return new RedisSlidingWindowRateLimiter(redisTemplate, perMinute, windowMillis);
        }
        return new RedisFixedWindowRateLimiter(redisTemplate, perMinute, windowSeconds);
    }

    @Bean
    @Primary
    public IOrderTrackingBus trackingBus(RedisConnectionFactory redisConnectionFactory,
                                          StringRedisTemplate redisTemplate) {
        if (redisConnectionFactory != null) {
            return new RedisOrderTrackingBus(redisTemplate);
        }
        return new NullOrderTrackingBus();
    }

    @Bean
    public InMemoryFallbackCacheService inMemoryFallbackCacheService() {
        return new InMemoryFallbackCacheService();
    }

    @Bean
    @ConditionalOnMissingBean(RedisConnectionFactory.class)
    public RedisConnectionFactory redisConnectionFactory(
            @Value("${spring.data.redis.host:localhost}") String host,
            @Value("${spring.data.redis.port:6379}") int port) {
        return new LettuceConnectionFactory(host, port);
    }

    @Bean
    @ConditionalOnMissingBean(StringRedisTemplate.class)
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        return new StringRedisTemplate(redisConnectionFactory);
    }
}
