package com.backend.movie_ticket_booking_system.convertor;

import com.backend.movie_ticket_booking_system.entities.Theater;
import com.backend.movie_ticket_booking_system.request.TheaterRequest;

public class TheaterConvertor {

    public static Theater theaterDtoToTheater(TheaterRequest theaterRequest) {
        return Theater.builder()
                .name(theaterRequest.getName())
                .address(theaterRequest.getAddress())
                .city(theaterRequest.getCity())
                .state(theaterRequest.getState())
                .country(theaterRequest.getCountry())
                .isActive(true)
                .build();
    }
}
