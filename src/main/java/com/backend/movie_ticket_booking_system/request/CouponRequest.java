package com.backend.movie_ticket_booking_system.request;

import lombok.Data;
import java.util.Date;

@Data
public class CouponRequest {
    private String code;
    private Double discountPercent;
    private Integer maxUses;
    private Date expiresAt;
}
