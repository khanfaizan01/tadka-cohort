package com.tadka.infrastructure.realtime;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A tracking event with a sequence number for replay on reconnect (ADR-020).
 */
public record SequencedTrackingEvent(long seq, OrderTrackingEvent event) {}
