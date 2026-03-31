package com.backend.movie_ticket_booking_system.services;

import com.backend.movie_ticket_booking_system.entities.Coupon;
import com.backend.movie_ticket_booking_system.entities.Movie;
import com.backend.movie_ticket_booking_system.repositories.CouponRepository;
import com.backend.movie_ticket_booking_system.repositories.MovieRepository;
import com.backend.movie_ticket_booking_system.request.CouponRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CouponService {
    private final CouponRepository couponRepository;
    private final MovieRepository movieRepository;

    public Coupon createCoupon(CouponRequest req) {
        if (couponRepository.findByCode(req.getCode().toUpperCase().trim()).isPresent()) {
            throw new RuntimeException("Coupon code already exists");
        }
        
        List<Movie> applicableMovies = new ArrayList<>();
        if (req.getMovieIds() != null && !req.getMovieIds().isEmpty()) {
            applicableMovies = movieRepository.findAllById(req.getMovieIds());
        }
        
        Coupon coupon = Coupon.builder()
                .code(req.getCode().toUpperCase().trim())
                .discountPercent(req.getDiscountPercent())
                .maxUses(req.getMaxUses())
                .expiresAt(req.getExpiresAt())
                .isActive(true)
                .usedCount(0)
                .applicableMovies(applicableMovies)
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
        return validateCouponForMovie(code, null);
    }

    public Double validateCouponForMovie(String code, Integer movieId) {
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
        
        // Check if coupon is movie-specific
        if (coupon.getApplicableMovies() != null && !coupon.getApplicableMovies().isEmpty()) {
            if (movieId == null) {
                // If we don't know the movie, just return the discount (backwards compat)
                return coupon.getDiscountPercent();
            }
            boolean movieMatch = coupon.getApplicableMovies().stream()
                    .anyMatch(m -> m.getId().equals(movieId));
            if (!movieMatch) {
                throw new RuntimeException("Mã giảm giá không áp dụng cho phim này");
            }
        }
        
        return coupon.getDiscountPercent();
    }

    public String updateCoupon(Integer id, CouponRequest req) {
        Coupon coupon = couponRepository.findById(id).orElseThrow(() -> new RuntimeException("Coupon not found"));
        
        if (req.getCode() != null) {
            String newCode = req.getCode().toUpperCase().trim();
            Optional<Coupon> existing = couponRepository.findByCode(newCode);
            if (existing.isPresent() && !existing.get().getId().equals(id)) {
                throw new RuntimeException("Coupon code already exists");
            }
            coupon.setCode(newCode);
        }
        
        if (req.getDiscountPercent() != null) {
            coupon.setDiscountPercent(req.getDiscountPercent());
        }
        
        if (req.getMaxUses() != null) {
            coupon.setMaxUses(req.getMaxUses());
        }
        
        coupon.setExpiresAt(req.getExpiresAt());

        if (req.getMovieIds() != null) {
            List<Movie> applicableMovies = movieRepository.findAllById(req.getMovieIds());
            coupon.setApplicableMovies(applicableMovies);
        }

        couponRepository.save(coupon);
        return "Coupon updated successfully";
    }

    public void applyCouponInfo(String code) {
        if (code == null || code.trim().isEmpty()) return;
        couponRepository.findByCode(code.toUpperCase().trim()).ifPresent(coupon -> {
            coupon.setUsedCount(coupon.getUsedCount() + 1);
            couponRepository.save(coupon);
        });
    }
}
