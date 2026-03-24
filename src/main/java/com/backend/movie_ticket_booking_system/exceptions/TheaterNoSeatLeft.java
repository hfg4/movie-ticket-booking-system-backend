package com.backend.movie_ticket_booking_system.exceptions;

public class TheaterNoSeatLeft extends RuntimeException {
    public TheaterNoSeatLeft() {
        super("Theater has no seats left for this show");
    }
}
