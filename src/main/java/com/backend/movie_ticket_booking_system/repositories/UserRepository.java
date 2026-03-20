package com.backend.movie_ticket_booking_system.repositories;

import com.backend.movie_ticket_booking_system.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmailAndIsActiveTrue(String email);
    @SuppressWarnings("unused")
    List<User> findAllByIsActiveTrue();
    
    Optional<User> findByIdAndIsActiveTrue(Integer id);

    Optional<User> findByEmail(String email);

}