package com.backend.movie_ticket_booking_system.exceptions;

import java.io.Serial;

public class ShowDoesNotExist extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -4436119261176031165L;

    public ShowDoesNotExist() {
        super("Show does not exists");
    }
}
