package com.backend.movie_ticket_booking_system.exceptions;

import java.io.Serial;

public class TheaterIsExist extends RuntimeException{
    @Serial
    private static final long serialVersionUID = 6386810783666583528L;

    public TheaterIsExist() {
        super("Theater is already Present on this Address");
    }
}
