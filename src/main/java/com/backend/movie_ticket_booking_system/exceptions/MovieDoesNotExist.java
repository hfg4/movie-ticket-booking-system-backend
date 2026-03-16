package com.backend.movie_ticket_booking_system.exceptions;

import java.io.Serial;

public class MovieDoesNotExist extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -5385129013790060351L;

    public MovieDoesNotExist() {
        super("Movie dose not Exists");
    }
}
