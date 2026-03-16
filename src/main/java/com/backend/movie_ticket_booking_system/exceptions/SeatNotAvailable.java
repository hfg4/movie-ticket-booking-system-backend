package com.backend.movie_ticket_booking_system.exceptions;

import java.io.Serial;

public class SeatNotAvailable extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1497113945165128412L;

    public SeatNotAvailable() {
        super("Requested Seats Are Not Available");
    }
}