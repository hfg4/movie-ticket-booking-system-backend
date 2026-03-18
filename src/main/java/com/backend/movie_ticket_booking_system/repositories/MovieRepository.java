package com.backend.movie_ticket_booking_system.repositories;

import com.backend.movie_ticket_booking_system.entities.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

public interface MovieRepository extends JpaRepository<Movie, Integer> {
    Movie findByMovieNameAndIsDeletedFalse(String name);
    List<Movie> findAllByIsDeletedFalse(Sort sort);
    Page<Movie> findAllByIsDeletedFalse(Pageable pageable);
    List<Movie> findAllByIsDeletedFalse();
    Optional<Movie> findByIdAndIsDeletedFalse(Integer id);
}