package com.backend.movie_ticket_booking_system.repositories;

import com.backend.movie_ticket_booking_system.entities.ShowSeat;
import com.backend.movie_ticket_booking_system.entities.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket,Integer> {

    @Query("SELECT ss FROM ShowSeat ss WHERE ss.show.showId = :showId AND ss.id NOT IN (SELECT ts.id FROM Ticket t JOIN t.showSeats ts WHERE t.show.showId = :showId)")
    List<ShowSeat> findAvailableSeatsByShowId(@Param("showId") Integer showId);
}
