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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final ShowRepository showRepository;
    private final UserRepository userRepository;

    @Transactional
    public TicketResponse ticketBooking(TicketRequest ticketRequest) {
        Show show = showRepository.findById(ticketRequest.getShowId())
                .orElseThrow(ShowDoesNotExist::new);

        User user = userRepository.findById(ticketRequest.getUserId())
                .orElseThrow(UserDoesNotExist::new);

        // Lock seats to prevent race conditions
        List<ShowSeat> requestedSeats = ticketRepository.findAndLockByShowIdAndSeatNos(
                show.getShowId(), ticketRequest.getRequestSeats());

        if (requestedSeats.size() != ticketRequest.getRequestSeats().size()) {
            throw new SeatNotAvailable();
        }

        // Verify if any of the locked seats are already booked (isAvailable = false)
        for (ShowSeat rs : requestedSeats) {
            if (Boolean.FALSE.equals(rs.getIsAvailable())) {
                throw new SeatNotAvailable();
            }
        }

        Double totalAmount = requestedSeats.stream()
                .mapToDouble(ShowSeat::getPrice)
                .sum();

        // Mark seats as unavailable
        for (ShowSeat ss : requestedSeats) {
            ss.setIsAvailable(Boolean.FALSE);
        }

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
        return ticketRepository.findByUser_Id(userId);
    }

    @Transactional
    public String rateTicket(Integer ticketId, Integer rating) {
        Optional<Ticket> ticketOpt = ticketRepository.findById(ticketId);

        if (ticketOpt.isEmpty()) {
            throw new TicketDoesNotExist();
        }

        Ticket ticket = ticketOpt.get();
        ticket.setRating(rating);
        ticketRepository.save(ticket);

        return "Ticket rated successfully";
    }

    @Transactional
    public String cancelTicket(Integer ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(TicketDoesNotExist::new);

        ticketRepository.delete(ticket);

        return "Ticket cancelled successfully";
    }

}
