package com.tadka.repository;

import com.tadka.domain.orders.CouponRedemption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.UUID;

public interface CouponRedemptionRepository extends JpaRepository<CouponRedemption, UUID> {

    @Query("DELETE FROM CouponRedemption cr WHERE cr.couponId = :couponId")
    void deleteByCouponId(@Param("couponId") UUID couponId);
}
