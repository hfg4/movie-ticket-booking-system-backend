package com.backend.movie_ticket_booking_system.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.movie_ticket_booking_system.entities.ShowSeat;

public interface ShowSeatRepository extends JpaRepository<ShowSeat, Integer> {
}
