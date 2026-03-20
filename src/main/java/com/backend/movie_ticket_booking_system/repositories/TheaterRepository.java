package com.backend.movie_ticket_booking_system.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.movie_ticket_booking_system.entities.Theater;

import java.util.List;
import java.util.Optional;

public interface TheaterRepository extends JpaRepository<Theater, Integer> {
    Theater findByAddressAndIsActiveTrue(String address);
    Theater findByAddress(String address);
    List<Theater> findAllByIsActiveTrue();
    Optional<Theater> findByIdAndIsActiveTrue(Integer id);
}
