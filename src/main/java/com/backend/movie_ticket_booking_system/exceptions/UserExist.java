package com.backend.movie_ticket_booking_system.exceptions;

import java.io.Serial;

public class UserExist extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -4666349320340656440L;

    public UserExist() {
        super("User Already Exists with this EmailId");
    }
}
