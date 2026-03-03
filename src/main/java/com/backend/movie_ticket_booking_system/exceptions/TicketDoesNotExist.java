package com.backend.movie_ticket_booking_system.exceptions;

public class TicketDoesNotExist extends RuntimeException {
    public TicketDoesNotExist() {
        super("Ticket does not exist");
    }

    public TicketDoesNotExist(String message) {
        super(message);
    }
}
