package com.backend.movie_ticket_booking_system.exceptions;

public class TheaterIsClosed extends RuntimeException {
    public TheaterIsClosed() {
        super("Theater is currently closed to prepare for the show");
    }
}
