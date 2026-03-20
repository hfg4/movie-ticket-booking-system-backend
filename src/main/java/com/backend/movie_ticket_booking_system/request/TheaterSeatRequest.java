package com.backend.movie_ticket_booking_system.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TheaterSeatRequest {
    @NotBlank(message = "Address is required")
    private String address;

    @NotNull(message = "No of seat in row is required")
    private Integer noOfSeatInRow;

    @NotNull(message = "No of premium seat is required")
    private Integer noOfPremiumSeat;

    @NotNull(message = "No of classic seat is required")
    private Integer noOfClassicSeat;
}
