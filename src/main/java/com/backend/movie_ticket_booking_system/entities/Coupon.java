package com.backend.movie_ticket_booking_system.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false)
    private Double discountPercent;

    private Integer maxUses;
    
    @Builder.Default
    private Integer usedCount = 0;

    private Date expiresAt;

    @Builder.Default
    private Boolean isActive = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "coupon_movies",
        joinColumns = @JoinColumn(name = "coupon_id"),
        inverseJoinColumns = @JoinColumn(name = "movie_id")
    )
    @Builder.Default
    private List<Movie> applicableMovies = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Timestamp createdAt;
}
