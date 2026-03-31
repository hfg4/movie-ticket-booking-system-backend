package com.backend.movie_ticket_booking_system.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notification_reads")
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer userId;

    @Column(nullable = false)
    private String notificationId;
}
