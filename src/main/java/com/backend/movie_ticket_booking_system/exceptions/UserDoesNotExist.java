package com.backend.movie_ticket_booking_system.exceptions;

import java.io.Serial;

public class UserDoesNotExist extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 264309547420961862L;

    public UserDoesNotExist() {
        super("User does not exists");
    }
}
