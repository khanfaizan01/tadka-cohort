package com.tadka.domain.payments;

import com.tadka.domain.valueobjects.Money;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments", schema = "payment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "amount_amount")),
        @AttributeOverride(name = "currency", column = @Column(name = "amount_currency"))
    })
    private Money amount;

    @Column(nullable = false)
    private String method;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private PaymentStatus status;

    @Column(name = "gateway_reference", nullable = true)
    private String gatewayReference;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
