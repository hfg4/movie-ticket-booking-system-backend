package com.backend.movie_ticket_booking_system.repositories;

import com.backend.movie_ticket_booking_system.entities.ShowSeat;
import com.backend.movie_ticket_booking_system.entities.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket,Integer> {
    List<Ticket> findByUser_Id(Integer userId);

    @Query("SELECT ss FROM ShowSeat ss WHERE ss.show.showId = :showId AND ss.id NOT IN (SELECT ts.id FROM Ticket t JOIN t.showSeats ts WHERE t.show.showId = :showId)")
    List<ShowSeat> findAvailableSeatsByShowId(@Param("showId") Integer showId);

    @org.springframework.data.jpa.repository.Query("SELECT AVG(t.rating) FROM Ticket t WHERE t.show.movie.id = :movieId AND t.rating IS NOT NULL")
    Double getAverageRatingForMovie(Integer movieId);
}
