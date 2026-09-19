package com.tadka.infrastructure.realtime;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Redis pub/sub implementation of the live-tracking backplane (ADR-020).
 * Also maintains a short, capped recent-events buffer per order (Redis LIST,
 * last 20 events, 6h TTL) so a reconnecting SSE client can replay what it missed.
 */
public class RedisOrderTrackingBus implements IOrderTrackingBus {

    private static final Logger log = LoggerFactory.getLogger(RedisOrderTrackingBus.class);
    private static final int RECENT_EVENTS_CAPACITY = 20;
    private static final Duration BUFFER_TTL = Duration.ofHours(6);
    private final ObjectMapper mapper = new ObjectMapper();

    private final StringRedisTemplate redis;

    public RedisOrderTrackingBus(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public boolean isEnabled() {
        try {
            org.springframework.data.redis.connection.RedisConnection conn = redis.getConnectionFactory().getConnection();
            try {
                String result = conn.ping();
                return "PONG".equalsIgnoreCase(result);
            } finally {
                conn.close();
            }
        } catch (DataAccessException e) {
            log.warn("Redis unavailable for liveness check: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public CompletableFuture<Void> publishAsync(OrderTrackingEvent trackingEvent) {
        try {
            String seqKey = "order:" + trackingEvent.orderId() + ":seq";
            String recentKey = "order:" + trackingEvent.orderId() + ":recent";

            long seq = redis.opsForValue().increment(seqKey);
            redis.expire(seqKey, BUFFER_TTL);

            String payload;
            try { payload = mapper.writeValueAsString(new SequencedTrackingEvent(seq, trackingEvent)); }
            catch (JsonProcessingException e) { throw new RuntimeException(e); }

            redis.opsForList().rightPush(recentKey, payload);
            redis.opsForList().trim(recentKey, -RECENT_EVENTS_CAPACITY, -1);
            redis.expire(recentKey, BUFFER_TTL);

            redis.convertAndSend("order:" + trackingEvent.orderId(), payload);
        } catch (DataAccessException ex) {
            log.warn("Redis unavailable for publishing tracking event for order {}.", trackingEvent.orderId());
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> subscribe(UUID orderId, SequencedTrackingEvent event) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public List<SequencedTrackingEvent> getEventsSince(UUID orderId, long sinceSeq) {
        try {
            String recentKey = "order:" + orderId + ":recent";
            List<String> raw = redis.opsForList().range(recentKey, 0, RECENT_EVENTS_CAPACITY - 1);
            List<SequencedTrackingEvent> events = new ArrayList<>();
            if (raw == null) return events;

            for (String entry : raw) {
                try {
                    SequencedTrackingEvent sequenced = mapper.readValue(entry, SequencedTrackingEvent.class);
                    if (sequenced.seq() > sinceSeq) events.add(sequenced);
                } catch (JsonProcessingException e) { /* skip */ }
            }
            return events;
        } catch (DataAccessException ex) {
            log.warn("Redis unavailable for replay buffer read on order {}.", orderId);
            return List.of();
        }
    }
}
