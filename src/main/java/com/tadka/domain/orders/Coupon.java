package com.tadka.domain.orders;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "coupons", schema = "ordering")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Coupon {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(name = "max_redemptions", nullable = false)
    private int maxRedemptions;

    @Column(nullable = false)
    private int redeemed;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Transient
    public int getRemaining() { return maxRedemptions - redeemed; }

    @Transient
    public boolean isExhausted() { return redeemed >= maxRedemptions; }
}
