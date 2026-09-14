package com.tadka.domain.orders;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "idempotency_keys", schema = "ordering")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IdempotencyKey {

    @Id
    @Column(length = 100)
    private String key;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
