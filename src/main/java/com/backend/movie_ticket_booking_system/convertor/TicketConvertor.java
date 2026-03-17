package com.backend.movie_ticket_booking_system.convertor;

import com.backend.movie_ticket_booking_system.entities.Show;
import com.backend.movie_ticket_booking_system.entities.Ticket;
import com.backend.movie_ticket_booking_system.response.TicketResponse;

import java.util.stream.Collectors;

public class TicketConvertor {

    public static TicketResponse returnTicket(Show show, Ticket ticket) {

        return TicketResponse.builder()
                .bookedSeats(ticket.getShowSeats().stream()
                        .map(ss -> ss.getTheaterSeat().getSeatNo())
                        .collect(Collectors.toList()))
                .address(show.getTheater().getAddress())
                .theaterName(show.getTheater().getName())
                .movieName(show.getMovie().getMovieName())
                .date(show.getShowDate())
                .time(show.getShowTime())
                .totalPrice(ticket.getTotalTicketsPrice())
                .confirmationNumber(ticket.getConfirmationNumber())
                .build();
    }
}
