package com.backend.movie_ticket_booking_system.exceptions;

import java.io.Serial;

public class MovieAlreadyExist extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 87214071728310561L;

    public MovieAlreadyExist() {
        super("Movie is already exists with same name and language");
    }
}
