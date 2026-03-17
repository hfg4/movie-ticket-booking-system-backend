package com.backend.movie_ticket_booking_system.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.sql.Time;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponse {
    private Time time;
    private Date date;
    private String movieName;
    private String theaterName;
    private String address;
    private List<String> bookedSeats;
    private Double totalPrice;
    private String confirmationNumber;
}