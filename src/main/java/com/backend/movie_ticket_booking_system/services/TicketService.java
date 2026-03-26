package com.backend.movie_ticket_booking_system.services;

import com.backend.movie_ticket_booking_system.convertor.TicketConvertor;
import com.backend.movie_ticket_booking_system.entities.Show;
import com.backend.movie_ticket_booking_system.entities.ShowSeat;
import com.backend.movie_ticket_booking_system.entities.Ticket;
import com.backend.movie_ticket_booking_system.entities.User;
import com.backend.movie_ticket_booking_system.exceptions.SeatNotAvailable;
import com.backend.movie_ticket_booking_system.exceptions.ShowDoesNotExist;
import com.backend.movie_ticket_booking_system.exceptions.TheaterIsClosed;
import com.backend.movie_ticket_booking_system.exceptions.TheaterNoSeatLeft;
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

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
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

        // Check if the show time is too close (e.g., within 30 minutes)
        java.time.LocalDate date = show.getShowDate().toLocalDate();
        LocalTime time = show.getShowTime().toLocalTime();
        LocalDateTime showDateTime = LocalDateTime.of(date, time);
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        
        // Prevent booking if show already started or is within 30 minutes
        if (now.plusMinutes(30).isAfter(showDateTime)) {
            throw new TheaterIsClosed();
        }

        User user = userRepository.findById(ticketRequest.getUserId())
                .orElseThrow(UserDoesNotExist::new);

        // Lock seats to prevent race conditions
        List<ShowSeat> requestedSeats = ticketRepository.findAndLockByShowIdAndSeatNos(
                show.getShowId(), ticketRequest.getRequestSeats());

        if (requestedSeats.size() != ticketRequest.getRequestSeats().size()) {
            throw new SeatNotAvailable();
        }
        
        // Count available seats to check if theater has no seats left
        long totalAvailableSeats = show.getShowSeatList().stream()
                .filter(ShowSeat::getIsAvailable)
                .count();
                
        if (totalAvailableSeats < ticketRequest.getRequestSeats().size()) {
            throw new TheaterNoSeatLeft();
        }

        // Verify if any of the locked seats are already booked (isAvailable = false) or held by someone else
        for (ShowSeat rs : requestedSeats) {
            if (Boolean.FALSE.equals(rs.getIsAvailable())) {
                throw new SeatNotAvailable();
            }
            if (rs.getHeldUntil() != null && now.isBefore(rs.getHeldUntil())) {
                if (!rs.getHeldByUserId().equals(user.getId())) {
                    throw new SeatNotAvailable(); // held by someone else
                }
            }
        }

        Double totalAmount = requestedSeats.stream()
                .mapToDouble(ShowSeat::getPrice)
                .sum();

        // Mark seats as unavailable and clear hold data
        for (ShowSeat ss : requestedSeats) {
            ss.setIsAvailable(Boolean.FALSE);
            ss.setHeldUntil(null);
            ss.setHeldByUserId(null);
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

        // Check if it's too late to cancel (e.g., within 1 hour before show or already started)
        Show show = ticket.getShow();
        java.time.LocalDate date = show.getShowDate().toLocalDate();
        LocalTime time = show.getShowTime().toLocalTime();
        LocalDateTime showDateTime = LocalDateTime.of(date, time);
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());

        if (now.plusHours(1).isAfter(showDateTime)) {
            throw new RuntimeException("Cannot cancel ticket within 1 hour before the show starts or after it has started.");
        }
        
        // Mark all seats associated with this ticket as available again
        List<ShowSeat> showSeats = ticket.getShowSeats();
        for (ShowSeat seat : showSeats) {
            seat.setIsAvailable(Boolean.TRUE);
        }

        // Remove the ticket reference from the user and show lists 
        User user = ticket.getUser();
        if (user != null) {
            user.getTicketList().remove(ticket);
        }
        
        show.getTicketList().remove(ticket);

        // Disconnect the show seats from the ticket since it's a many-to-many managed by the ticket
        ticket.getShowSeats().clear();

        ticketRepository.delete(ticket);

        return "Ticket cancelled successfully";
    }

    @Transactional
    public String holdSeats(TicketRequest ticketRequest) {
        Show show = showRepository.findById(ticketRequest.getShowId())
                .orElseThrow(ShowDoesNotExist::new);
        User user = userRepository.findById(ticketRequest.getUserId())
                .orElseThrow(UserDoesNotExist::new);

        List<ShowSeat> requestedSeats = ticketRepository.findAndLockByShowIdAndSeatNos(
                show.getShowId(), ticketRequest.getRequestSeats());

        if (requestedSeats.size() != ticketRequest.getRequestSeats().size()) {
            throw new SeatNotAvailable();
        }

        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        for (ShowSeat ss : requestedSeats) {
            if (Boolean.FALSE.equals(ss.getIsAvailable())) {
                throw new SeatNotAvailable();
            }
            if (ss.getHeldUntil() != null && now.isBefore(ss.getHeldUntil())) {
                if (!ss.getHeldByUserId().equals(user.getId())) {
                    throw new SeatNotAvailable(); // held by someone else
                }
            }
        }

        java.time.LocalDateTime holdEnd = now.plusMinutes(1);
        for (ShowSeat ss : requestedSeats) {
            ss.setHeldUntil(holdEnd);
            ss.setHeldByUserId(user.getId());
        }
        
        return String.valueOf(holdEnd.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli());
    }

    @Transactional
    public String releaseSeats(TicketRequest ticketRequest) {
        List<ShowSeat> requestedSeats = ticketRepository.findAndLockByShowIdAndSeatNos(
                ticketRequest.getShowId(), ticketRequest.getRequestSeats());

        for (ShowSeat ss : requestedSeats) {
            if (ss.getHeldByUserId() != null && ss.getHeldByUserId().equals(ticketRequest.getUserId())) {
                ss.setHeldUntil(null);
                ss.setHeldByUserId(null);
            }
        }
        return "Đã hủy giữ ghế";
    }

}
