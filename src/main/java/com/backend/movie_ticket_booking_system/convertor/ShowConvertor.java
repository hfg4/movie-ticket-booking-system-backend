package com.backend.movie_ticket_booking_system.convertor;

import com.backend.movie_ticket_booking_system.entities.Show;
import com.backend.movie_ticket_booking_system.request.ShowRequest;

public class ShowConvertor {

    public static Show showDtoToShow(ShowRequest showRequest) {

        return Show.builder()
                .showTime(showRequest.getShowStartTime())
                .showDate(showRequest.getShowDate())
                .screenNumber(showRequest.getScreenNumber())
                .build();
    }
}
