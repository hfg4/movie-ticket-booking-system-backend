package com.backend.movie_ticket_booking_system.repositories;

import com.backend.movie_ticket_booking_system.entities.NotificationRead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationReadRepository extends JpaRepository<NotificationRead, Long> {
    List<NotificationRead> findByUserId(Integer userId);
    Optional<NotificationRead> findByUserIdAndNotificationId(Integer userId, String notificationId);
    boolean existsByUserIdAndNotificationId(Integer userId, String notificationId);
}
