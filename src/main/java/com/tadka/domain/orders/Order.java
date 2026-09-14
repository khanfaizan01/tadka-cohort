package com.tadka.domain.orders;

import com.tadka.domain.valueobjects.Address;
import com.tadka.domain.valueobjects.Money;
import com.tadka.domain.common.IDomainEvent;
import com.tadka.domain.common.Result;
import com.tadka.domain.orders.Events.OrderConfirmedEvent;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders", schema = "ordering")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "restaurant_id", nullable = false)
    private UUID restaurantId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private OrderStatus status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "total_amount_amount")),
        @AttributeOverride(name = "currency", column = @Column(name = "total_amount_currency"))
    })
    private Money totalAmount;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "line1", column = @Column(name = "delivery_address_line1")),
        @AttributeOverride(name = "line2", column = @Column(name = "delivery_address_line2")),
        @AttributeOverride(name = "city", column = @Column(name = "delivery_address_city")),
        @AttributeOverride(name = "pincode", column = @Column(name = "delivery_address_pincode")),
        @AttributeOverride(name = "latitude", column = @Column(name = "delivery_address_latitude")),
        @AttributeOverride(name = "longitude", column = @Column(name = "delivery_address_longitude"))
    })
    private Address deliveryAddress;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason")
    private String cancellationReason;

    // Optimistic concurrency (ADR-012). Hibernate manages this integer column
    // automatically: it increments on each UPDATE and checks affected-row-count
    // so concurrent modifications throw OptimisticLockingFailureException → 409.
    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    // PostgreSQL system column, kept for audit reference only.
    @Column(name = "xmin", insertable = false, updatable = false, nullable = false)
    private Long xmin;

    // Domain events raised by this aggregate, dispatched AFTER persistence (ADR-013).
    @Transient
    private List<IDomainEvent> domainEvents = new ArrayList<>();

    public void raise(IDomainEvent event) {
        domainEvents.add(event);
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }

    public List<IDomainEvent> getDomainEvents() {
        return domainEvents;
    }

    // DDD: encapsulate state transitions
    public Result transition(OrderStatus nextStatus) {
        if (!OrderStateMachine.canTransition(status, nextStatus)) {
            List<OrderStatus> allowed = OrderStateMachine.getAllowedTransitions(status);
            return Result.failure(
                "Cannot transition from '" + status + "' to '" + nextStatus + "'. Allowed: " + allowed);
        }
        this.status = nextStatus;
        if (nextStatus == OrderStatus.CONFIRMED) {
            this.confirmedAt = LocalDateTime.now();
            raise(new OrderConfirmedEvent(id, customerId));
        }
        if (nextStatus == OrderStatus.CANCELLED) {
            this.cancelledAt = LocalDateTime.now();
        }
        if (nextStatus == OrderStatus.DELIVERED) {
            this.deliveredAt = LocalDateTime.now();
        }
        return Result.success();
    }

    public Result cancel(String reason) {
        Result result = transition(OrderStatus.CANCELLED);
        if (result.isFailure()) return result;
        this.cancelledAt = LocalDateTime.now();
        this.cancellationReason = reason;
        return Result.success();
    }
}
