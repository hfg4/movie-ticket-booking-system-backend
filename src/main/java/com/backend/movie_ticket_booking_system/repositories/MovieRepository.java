package com.backend.movie_ticket_booking_system.repositories;

import com.backend.movie_ticket_booking_system.entities.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<Movie, Integer> {
    Movie findByMovieName(String name);
}