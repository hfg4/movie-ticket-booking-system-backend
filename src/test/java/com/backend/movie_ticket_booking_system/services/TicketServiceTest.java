package com.backend.movie_ticket_booking_system.services;

import com.backend.movie_ticket_booking_system.entities.*;
import com.backend.movie_ticket_booking_system.enums.SeatType;
import com.backend.movie_ticket_booking_system.exceptions.SeatNotAvailable;
import com.backend.movie_ticket_booking_system.exceptions.ShowDoesNotExist;
import com.backend.movie_ticket_booking_system.exceptions.TicketDoesNotExist;
import com.backend.movie_ticket_booking_system.exceptions.UserDoesNotExist;
import com.backend.movie_ticket_booking_system.repositories.ShowRepository;
import com.backend.movie_ticket_booking_system.repositories.TicketRepository;
import com.backend.movie_ticket_booking_system.repositories.UserRepository;
import com.backend.movie_ticket_booking_system.request.TicketRequest;
import com.backend.movie_ticket_booking_system.response.TicketResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;
import java.sql.Time;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * KIỂM THỬ HỘP TRẮNG - TicketService
 *
 * Kiểm tra các nhánh logic:
 * - ticketBooking: show/user check, seat availability, price calculation
 * - rateTicket: ticket exists/not
 * - cancelTicket: ticket exists/not
 */
@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private ShowRepository showRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TicketService ticketService;

    private Show testShow;
    private User testUser;
    private Theater testTheater;
    private Movie testMovie;
    private TheaterSeat theaterSeat1;
    private TheaterSeat theaterSeat2;
    private ShowSeat showSeat1;
    private ShowSeat showSeat2;
    private TicketRequest ticketRequest;

    @BeforeEach
    void setUp() {
        // Setup Theater
        testTheater = Theater.builder()
                .id(1)
                .name("CGV Vincom")
                .address("123 Nguyễn Huệ, Q.1, TP.HCM")
                .build();

        // Setup Movie
        testMovie = Movie.builder()
                .id(1)
                .movieName("Avengers: Endgame")
                .build();

        // Setup TheaterSeats
        theaterSeat1 = TheaterSeat.builder()
                .id(1)
                .seatNo("A1")
                .seatType(SeatType.STANDARD)
                .theater(testTheater)
                .build();

        theaterSeat2 = TheaterSeat.builder()
                .id(2)
                .seatNo("A2")
                .seatType(SeatType.STANDARD)
                .theater(testTheater)
                .build();

        // Setup Show
        testShow = Show.builder()
                .showId(1)
                .showDate(Date.valueOf("2024-12-25"))
                .showTime(Time.valueOf("19:00:00"))
                .movie(testMovie)
                .theater(testTheater)
                .build();

        // Setup ShowSeats
        showSeat1 = ShowSeat.builder()
                .id(1)
                .theaterSeat(theaterSeat1)
                .price(100000.0)
                .isAvailable(true)
                .show(testShow)
                .build();

        showSeat2 = ShowSeat.builder()
                .id(2)
                .theaterSeat(theaterSeat2)
                .price(100000.0)
                .isAvailable(true)
                .show(testShow)
                .build();

        // Setup User
        testUser = User.builder()
                .id(1)
                .name("Nguyen Van A")
                .email("test@cinema.com")
                .build();

        // Setup TicketRequest
        ticketRequest = new TicketRequest();
        ticketRequest.setShowId(1);
        ticketRequest.setUserId(1);
        ticketRequest.setRequestSeats(Arrays.asList("A1", "A2"));
    }

    // =========================================================================
    // WB21: ticketBooking() — Happy path: show, user, ghế available → tạo ticket
    // Branch: show found → user found → seats matched & available → save ticket
    // =========================================================================
    @Test
    @DisplayName("WB21 - ticketBooking: Đủ điều kiện → tạo vé thành công")
    void ticketBooking_WhenAllValid_ShouldCreateTicket() {
        // Arrange
        when(showRepository.findById(1)).thenReturn(Optional.of(testShow));
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(ticketRepository.findAndLockByShowIdAndSeatNos(1, Arrays.asList("A1", "A2")))
                .thenReturn(Arrays.asList(showSeat1, showSeat2));

        Ticket savedTicket = Ticket.builder()
                .ticketId(1)
                .totalTicketsPrice(200000.0)
                .confirmationNumber("TKT-123-1")
                .user(testUser)
                .show(testShow)
                .showSeats(Arrays.asList(showSeat1, showSeat2))
                .build();
        when(ticketRepository.save(any(Ticket.class))).thenReturn(savedTicket);

        // Act
        TicketResponse response = ticketService.ticketBooking(ticketRequest);

        // Assert
        assertNotNull(response);
        assertEquals(200000.0, response.getTotalPrice());
        assertEquals("Avengers: Endgame", response.getMovieName());
        assertEquals("CGV Vincom", response.getTheaterName());
        assertEquals(2, response.getBookedSeats().size());

        // Verify seats marked as unavailable
        assertFalse(showSeat1.getIsAvailable());
        assertFalse(showSeat2.getIsAvailable());
    }

    // =========================================================================
    // WB22: ticketBooking() — Show không tồn tại → throw ShowDoesNotExist
    // Branch: showRepository.findById → empty → throw
    // =========================================================================
    @Test
    @DisplayName("WB22 - ticketBooking: Show không tồn tại → throw ShowDoesNotExist")
    void ticketBooking_WhenShowNotFound_ShouldThrowException() {
        // Arrange
        when(showRepository.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ShowDoesNotExist.class, () -> ticketService.ticketBooking(ticketRequest));
    }

    // =========================================================================
    // WB23: ticketBooking() — Ghế không available → throw SeatNotAvailable
    // Branch: seat.isAvailable == false → throw
    // =========================================================================
    @Test
    @DisplayName("WB23 - ticketBooking: Ghế đã bị đặt → throw SeatNotAvailable")
    void ticketBooking_WhenSeatNotAvailable_ShouldThrowException() {
        // Arrange
        when(showRepository.findById(1)).thenReturn(Optional.of(testShow));
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));

        // Ghế A1 đã bị đặt (isAvailable = false)
        showSeat1.setIsAvailable(false);
        when(ticketRepository.findAndLockByShowIdAndSeatNos(1, Arrays.asList("A1", "A2")))
                .thenReturn(Arrays.asList(showSeat1, showSeat2));

        // Act & Assert
        assertThrows(SeatNotAvailable.class, () -> ticketService.ticketBooking(ticketRequest));
    }

    // =========================================================================
    // WB24: rateTicket() — Ticket tồn tại → cập nhật rating
    // Branch: ticketOpt.isPresent → set rating → save
    // =========================================================================
    @Test
    @DisplayName("WB24 - rateTicket: Ticket tồn tại → đánh giá thành công")
    void rateTicket_WhenTicketExists_ShouldUpdateRating() {
        // Arrange
        Ticket ticket = Ticket.builder()
                .ticketId(1)
                .totalTicketsPrice(100000.0)
                .build();
        when(ticketRepository.findById(1)).thenReturn(Optional.of(ticket));

        // Act
        String result = ticketService.rateTicket(1, 5);

        // Assert
        assertEquals("Ticket rated successfully", result);
        assertEquals(5, ticket.getRating());
        verify(ticketRepository, times(1)).save(ticket);
    }

    // =========================================================================
    // WB25: rateTicket() — Ticket không tồn tại → throw TicketDoesNotExist
    // Branch: ticketOpt.isEmpty → throw
    // =========================================================================
    @Test
    @DisplayName("WB25 - rateTicket: Ticket không tồn tại → throw TicketDoesNotExist")
    void rateTicket_WhenTicketNotFound_ShouldThrowException() {
        // Arrange
        when(ticketRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(TicketDoesNotExist.class, () -> ticketService.rateTicket(999, 5));
    }

    // =========================================================================
    // WB26: cancelTicket() — Ticket tồn tại → xóa ticket
    // Branch: ticket found → delete
    // =========================================================================
    @Test
    @DisplayName("WB26 - cancelTicket: Ticket tồn tại → hủy vé thành công")
    void cancelTicket_WhenTicketExists_ShouldDelete() {
        // Arrange
        Ticket ticket = Ticket.builder()
                .ticketId(1)
                .totalTicketsPrice(100000.0)
                .build();
        when(ticketRepository.findById(1)).thenReturn(Optional.of(ticket));

        // Act
        String result = ticketService.cancelTicket(1);

        // Assert
        assertEquals("Ticket cancelled successfully", result);
        verify(ticketRepository, times(1)).delete(ticket);
    }
}
