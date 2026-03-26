package com.backend.movie_ticket_booking_system.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class TicketRequest {
    @NotNull(message = "Show ID is required")
    private Integer showId;

    @NotNull(message = "User ID is required")
    private Integer userId;

    @NotEmpty(message = "At least one seat must be selected")
    private List<String> requestSeats;

    private String couponCode;
}
