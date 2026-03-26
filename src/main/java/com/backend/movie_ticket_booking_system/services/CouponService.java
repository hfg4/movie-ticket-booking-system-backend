package com.backend.movie_ticket_booking_system.services;

import com.backend.movie_ticket_booking_system.entities.Coupon;
import com.backend.movie_ticket_booking_system.repositories.CouponRepository;
import com.backend.movie_ticket_booking_system.request.CouponRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CouponService {
    private final CouponRepository couponRepository;

    public Coupon createCoupon(CouponRequest req) {
        if (couponRepository.findByCode(req.getCode().toUpperCase().trim()).isPresent()) {
            throw new RuntimeException("Coupon code already exists");
        }
        Coupon coupon = Coupon.builder()
                .code(req.getCode().toUpperCase().trim())
                .discountPercent(req.getDiscountPercent())
                .maxUses(req.getMaxUses())
                .expiresAt(req.getExpiresAt())
                .isActive(true)
                .usedCount(0)
                .build();
        return couponRepository.save(coupon);
    }

    public List<Coupon> getAllCoupons() {
        return couponRepository.findAll();
    }

    public String toggleCoupon(int id) {
        Coupon coupon = couponRepository.findById(id).orElseThrow(() -> new RuntimeException("Coupon not found"));
        coupon.setIsActive(!coupon.getIsActive());
        couponRepository.save(coupon);
        return "Coupon status updated";
    }

    public String deleteCoupon(int id) {
        couponRepository.deleteById(id);
        return "Coupon deleted successfully";
    }

    public Double validateCoupon(String code) {
        if (code == null || code.trim().isEmpty()) return 0.0;
        
        Optional<Coupon> opt = couponRepository.findByCode(code.toUpperCase().trim());
        if (opt.isEmpty()) throw new RuntimeException("Mã giảm giá không tồn tại");
        
        Coupon coupon = opt.get();
        if (!coupon.getIsActive()) throw new RuntimeException("Mã giảm giá đã bị vô hiệu hóa");
        
        if (coupon.getExpiresAt() != null && coupon.getExpiresAt().before(new Date())) {
            throw new RuntimeException("Mã giảm giá đã hết hạn");
        }
        
        if (coupon.getMaxUses() != null && coupon.getUsedCount() >= coupon.getMaxUses()) {
            throw new RuntimeException("Mã giảm giá đã hết lượt sử dụng");
        }
        
        return coupon.getDiscountPercent();
    }

    public void applyCouponInfo(String code) {
        if (code == null || code.trim().isEmpty()) return;
        couponRepository.findByCode(code.toUpperCase().trim()).ifPresent(coupon -> {
            coupon.setUsedCount(coupon.getUsedCount() + 1);
            couponRepository.save(coupon);
        });
    }
}
