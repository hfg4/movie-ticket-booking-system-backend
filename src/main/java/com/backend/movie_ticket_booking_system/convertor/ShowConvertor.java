package com.backend.movie_ticket_booking_system.convertor;

import com.backend.movie_ticket_booking_system.entities.Show;
import com.backend.movie_ticket_booking_system.request.ShowRequest;

import java.sql.Date;
import java.sql.Time;

public class ShowConvertor {

    private ShowConvertor() {
        // Private constructor to hide the implicit public one
    }

    public static Show showDtoToShow(ShowRequest showRequest) {
        if (showRequest == null) {
            return null;
        }

        return Show.builder()
                .showTime(Time.valueOf(showRequest.getShowStartTime()))
                .showDate(Date.valueOf(showRequest.getShowDate()))
                .screenNumber(showRequest.getScreenNumber())
                .build();
    }
}
