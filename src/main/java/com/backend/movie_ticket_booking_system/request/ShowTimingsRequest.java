package com.backend.movie_ticket_booking_system.request;


import lombok.Data;

import java.sql.Date;

@Data
@SuppressWarnings("unused")
public class ShowTimingsRequest {
    private Date date;
    private Integer theaterId;
    private Integer movieId;
}
