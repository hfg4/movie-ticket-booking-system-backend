package com.backend.movie_ticket_booking_system.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.sql.Date;
import java.sql.Time;

@Data
public class ShowRequest {

    @NotNull(message = "Show start time is required")
    private Time showStartTime;

    @NotNull(message = "Show date is required")
    private Date showDate;

    @NotNull(message = "Theater ID is required")
    private Integer theaterId;

    @NotNull(message = "Movie ID is required")
    private Integer movieId;
}
