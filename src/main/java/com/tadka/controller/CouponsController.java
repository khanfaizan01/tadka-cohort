package com.tadka.controller;

import com.tadka.controller.dto.RedeemRequest;
import com.tadka.domain.orders.Coupon;
import com.tadka.service.CouponService;
import jakarta.validation.Valid;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/coupons")
public class CouponsController {

    private final CouponService couponService;

    public CouponsController(CouponService couponService) {
        this.couponService = couponService;
    }

    @GetMapping("/{code}")
    public ResponseEntity<Coupon> getByCode(@PathVariable String code) {
        try {
            return ResponseEntity.ok(couponService.getByCode(code));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{code}/redeem/none")
    public ResponseEntity<Coupon> redeemNone(
            @PathVariable String code,
            @Valid @RequestBody RedeemRequest request) {
        try {
            return ResponseEntity.ok(couponService.redeemNone(code, request.getCustomerId()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(422).build();
        }
    }

    @PostMapping("/{code}/redeem/optimistic")
    public ResponseEntity<Coupon> redeemOptimistic(
            @PathVariable String code,
            @Valid @RequestBody RedeemRequest request) {
        try {
            return ResponseEntity.ok(couponService.redeemOptimistic(code, request.getCustomerId()));
        } catch (IllegalStateException | org.springframework.dao.OptimisticLockingFailureException e) {
            return ResponseEntity.status(409).build();
        }
    }

    @PostMapping("/{code}/redeem/pessimistic")
    public ResponseEntity<Coupon> redeemPessimistic(
            @PathVariable String code,
            @Valid @RequestBody RedeemRequest request) {
        try {
            return ResponseEntity.ok(couponService.redeemPessimistic(code, request.getCustomerId()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(422).build();
        }
    }

    @PostMapping("/{code}/reset")
    public ResponseEntity<Void> reset(@PathVariable String code) {
        couponService.reset(code);
        return ResponseEntity.ok().build();
    }
}
