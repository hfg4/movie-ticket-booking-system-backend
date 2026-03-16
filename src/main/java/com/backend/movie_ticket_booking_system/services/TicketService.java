package com.backend.movie_ticket_booking_system.services;

import com.backend.movie_ticket_booking_system.convertor.TicketConvertor;
import com.backend.movie_ticket_booking_system.entities.Show;
import com.backend.movie_ticket_booking_system.entities.ShowSeat;
import com.backend.movie_ticket_booking_system.entities.Ticket;
import com.backend.movie_ticket_booking_system.entities.User;
import com.backend.movie_ticket_booking_system.exceptions.SeatNotAvailable;
import com.backend.movie_ticket_booking_system.exceptions.ShowDoesNotExist;
import com.backend.movie_ticket_booking_system.exceptions.TicketDoesNotExist;
import com.backend.movie_ticket_booking_system.exceptions.UserDoesNotExist;
import com.backend.movie_ticket_booking_system.repositories.ShowRepository;
import com.backend.movie_ticket_booking_system.repositories.TicketRepository;
import com.backend.movie_ticket_booking_system.repositories.UserRepository;
import com.backend.movie_ticket_booking_system.request.TicketRequest;
import com.backend.movie_ticket_booking_system.response.TicketResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public TicketResponse ticketBooking(TicketRequest ticketRequest) {
        Show show = showRepository.findById(ticketRequest.getShowId())
                .orElseThrow(ShowDoesNotExist::new);

        User user = userRepository.findById(ticketRequest.getUserId())
                .orElseThrow(UserDoesNotExist::new);

        List<ShowSeat> availableSeats = ticketRepository.findAvailableSeatsByShowId(show.getShowId());
        List<ShowSeat> requestedSeats = show.getShowSeatList().stream()
                .filter(ss -> ticketRequest.getRequestSeats().contains(ss.getTheaterSeat().getSeatNo()))
                .collect(Collectors.toList());

        if (requestedSeats.size() != ticketRequest.getRequestSeats().size()) {
            throw new SeatNotAvailable();
        }

        for (ShowSeat rs : requestedSeats) {
            if (!availableSeats.contains(rs)) {
                throw new SeatNotAvailable();
            }
        }

        Double totalAmount = requestedSeats.stream()
                .mapToDouble(ShowSeat::getPrice)
                .sum();

        Ticket ticket = Ticket.builder()
                .totalTicketsPrice(totalAmount)
                .confirmationNumber("TKT-" + System.currentTimeMillis() + "-" + user.getId())
                .user(user)
                .show(show)
                .showSeats(requestedSeats)
                .build();

        ticket = ticketRepository.save(ticket);

        return TicketConvertor.returnTicket(show, ticket);
    }

    public Ticket getTicketById(Integer ticketId) {
        Optional<Ticket> ticketOpt = ticketRepository.findById(ticketId);

        if (ticketOpt.isEmpty()) {
            throw new TicketDoesNotExist();
        }

        return ticketOpt.get();
    }

    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    public List<Ticket> getTicketsByUserId(Integer userId) {
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isEmpty()) {
            throw new UserDoesNotExist();
        }

        return userOpt.get().getTicketList();
    }

    @Transactional
    public String cancelTicket(Integer ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(TicketDoesNotExist::new);

        ticketRepository.delete(ticket);

        return "Ticket cancelled successfully";
    }

}
