package com.backend.movie_ticket_booking_system.exceptions;

import java.io.Serial;

public class TheaterIsNotExist extends RuntimeException{
    @Serial
    private static final long serialVersionUID = -80039152090012599L;

    public TheaterIsNotExist() {
        super("Theater is not present in this address");
    }
}
