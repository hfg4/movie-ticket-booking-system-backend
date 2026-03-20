package com.backend.movie_ticket_booking_system.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ShowRequest {

    @NotNull(message = "Show start time is required")
    private LocalTime showStartTime;

    @NotNull(message = "Show date is required")
    private LocalDate showDate;

    @NotNull(message = "Theater ID is required")
    private Integer theaterId;

    @NotNull(message = "Movie ID is required")
    private Integer movieId;

    private Integer screenNumber;
}
