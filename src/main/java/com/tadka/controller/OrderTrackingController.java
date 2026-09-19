package com.tadka.controller;

import com.tadka.domain.orders.Order;
import com.tadka.infrastructure.realtime.OrderTrackingEvent;
import com.tadka.infrastructure.realtime.SequencedTrackingEvent;
import com.tadka.infrastructure.realtime.IOrderTrackingBus;
import com.tadka.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;
import java.util.concurrent.*;

/**
 * Day 6, Beat (ADR-020): Live order tracking via Server-Sent Events.
 * Streams status changes pushed over the Redis pub/sub backplane until the client disconnects.
 */
@RestController
@RequestMapping("/api/v1/orders")
public class OrderTrackingController {

    private static final Logger log = LoggerFactory.getLogger(OrderTrackingController.class);

    private final IOrderTrackingBus bus;
    private final OrderRepository orders;

    public OrderTrackingController(IOrderTrackingBus bus, OrderRepository orders) {
        this.bus = bus;
        this.orders = orders;
    }

    @GetMapping(value = "/{id}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter getEvents(@PathVariable UUID id,
                                 @RequestHeader(value = "Last-Event-ID", required = false) String lastEventId) {
        if (!bus.isEnabled()) {
            SseEmitter emitter = new SseEmitter(5000L);
            emitter.completeWithError(new IllegalStateException("Live tracking requires Redis (ADR-020)"));
            return emitter;
        }

        SseEmitter emitter = new SseEmitter(5 * 60 * 1000L);
        emitter.onCompletion(() -> log.info("SSE connection closed for order {}", id));
        emitter.onTimeout(() -> log.info("SSE connection timed out for order {}", id));

        // Replay missed events on reconnect (use array holder for lambda access)
        long[] replayedThrough = {0L};
        if (lastEventId != null) {
            try {
                long sinceSeq = Long.parseLong(lastEventId);
                for (SequencedTrackingEvent sequenced : bus.getEventsSince(id, sinceSeq)) {
                    writeEvent(emitter, sequenced);
                    replayedThrough[0] = sequenced.seq();
                }
            } catch (NumberFormatException e) { /* fresh connect, no replay */ }
        }

        // If no reconnect, send current status immediately
        if (replayedThrough[0] == 0 && lastEventId == null) {
            Order order = orders.findByIdWithItems(id);
            if (order != null) {
                writeEvent(emitter, new SequencedTrackingEvent(0,
                    new OrderTrackingEvent(id, order.getStatus().name(),
                        "Current status: " + order.getStatus(), java.time.LocalDateTime.now())));
            }
        }

        // Poll for new events periodically
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                for (SequencedTrackingEvent seq : bus.getEventsSince(id, replayedThrough[0])) {
                    if (seq.seq() > replayedThrough[0]) {
                        writeEvent(emitter, seq);
                        replayedThrough[0] = seq.seq();
                    }
                }
            } catch (Exception e) { /* ignore */ }
        }, 1, 1, TimeUnit.SECONDS);

        emitter.onCompletion(() -> scheduler.shutdown());
        return emitter;
    }

    private void writeEvent(SseEmitter emitter, SequencedTrackingEvent sequenced) {
        try {
            emitter.send(SseEmitter.event()
                .id(String.valueOf(sequenced.seq()))
                .name(sequenced.event().status())
                .data(sequenced.event().toString()));
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
    }
}
