package com.tadka.service;

import com.tadka.domain.orders.Coupon;
import com.tadka.domain.orders.CouponRedemption;
import com.tadka.repository.CouponRepository;
import com.tadka.repository.CouponRedemptionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@Transactional
public class CouponService {

    private final CouponRepository couponRepository;
    private final CouponRedemptionRepository couponRedemptionRepository;

    public CouponService(CouponRepository couponRepository,
                         CouponRedemptionRepository couponRedemptionRepository) {
        this.couponRepository = couponRepository;
        this.couponRedemptionRepository = couponRedemptionRepository;
    }

    @Transactional(readOnly = true)
    public Coupon getByCode(String code) {
        return couponRepository.findByCode(code)
            .orElseThrow(() -> new EntityNotFoundException("Coupon not found"));
    }

    @Transactional
    public Coupon redeemNone(String code, UUID customerId) {
        Coupon coupon = getByCode(code);
        if (coupon.isExhausted()) {
            throw new IllegalStateException("Coupon '" + code + "' is exhausted.");
        }

        // BROKEN: read-then-write with no concurrency guard (ADR-045 demo)
        int newRedeemed = coupon.getRedeemed() + 1;
        coupon.setRedeemed(newRedeemed);
        couponRedemptionRepository.save(new CouponRedemption(
            UUID.randomUUID(), coupon.getId(), customerId, java.time.LocalDateTime.now()));
        return couponRepository.save(coupon);
    }

    @Transactional
    public Coupon redeemOptimistic(String code, UUID customerId) {
        Coupon coupon = getByCode(code);
        if (coupon.isExhausted()) {
            throw new IllegalStateException("Coupon '" + code + "' is exhausted.");
        }

        // Optimistic concurrency via xmin (ADR-012)
        coupon.setRedeemed(coupon.getRedeemed() + 1);
        couponRedemptionRepository.save(new CouponRedemption(
            UUID.randomUUID(), coupon.getId(), customerId, java.time.LocalDateTime.now()));

        try {
            return couponRepository.save(coupon);
        } catch (OptimisticLockingFailureException e) {
            throw new OptimisticLockingFailureException("Concurrent redemption — retry");
        }
    }

    @Transactional
    public Coupon redeemPessimistic(String code, UUID customerId) {
        Coupon coupon = getByCode(code);
        if (coupon.isExhausted()) {
            throw new IllegalStateException("Coupon '" + code + "' is exhausted.");
        }

        coupon.setRedeemed(coupon.getRedeemed() + 1);
        couponRedemptionRepository.save(new CouponRedemption(
            UUID.randomUUID(), coupon.getId(), customerId, java.time.LocalDateTime.now()));
        return couponRepository.save(coupon);
    }

    @Transactional
    public void reset(String code) {
        Coupon coupon = getByCode(code);
        coupon.setRedeemed(0);
        couponRepository.save(coupon);
        couponRedemptionRepository.deleteByCouponId(coupon.getId());
    }
}
