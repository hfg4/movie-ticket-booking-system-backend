package com.backend.movie_ticket_booking_system.controllers;

import com.backend.movie_ticket_booking_system.request.CouponRequest;
import com.backend.movie_ticket_booking_system.services.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/coupon")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @PostMapping("/addNew")
    public ResponseEntity<?> createCoupon(@RequestBody CouponRequest req) {
        try {
            return ResponseEntity.ok(couponService.createCoupon(req));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody CouponRequest req) {
        try {
            return ResponseEntity.ok(couponService.updateCoupon(id, req));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(couponService.getAllCoupons());
    }

    @PutMapping("/{id}/toggle")
    public ResponseEntity<?> toggle(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(couponService.toggleCoupon(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(couponService.deleteCoupon(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validate(@RequestParam String code, @RequestParam(required = false) Integer movieId) {
        try {
            Double discount = couponService.validateCouponForMovie(code, movieId);
            return ResponseEntity.ok(discount);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
