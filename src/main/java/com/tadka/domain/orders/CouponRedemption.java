package com.tadka.domain.orders;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "coupon_redemptions", schema = "ordering")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CouponRedemption {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "coupon_id", nullable = false)
    private UUID couponId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "redeemed_at", nullable = false)
    private LocalDateTime redeemedAt;
}
