package com.backend.movie_ticket_booking_system.entities;

import com.backend.movie_ticket_booking_system.enums.ShowStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.proxy.HibernateProxy;

import java.sql.Timestamp;
import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "SHOWS")
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Show {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer showId;

    private Time showTime;

    private Date showDate;

    private Integer screenNumber;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ShowStatus status = ShowStatus.ACTIVE;

    @Builder.Default
    private Boolean isDeleted = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Timestamp createdAt;

    @ManyToOne
    @JoinColumn
    private Movie movie;

    @ManyToOne
    @JoinColumn
    private Theater theater;

    @OneToMany(mappedBy = "show", cascade = CascadeType.ALL)
    @ToString.Exclude
    @Builder.Default
    @JsonIgnore
    private List<ShowSeat> showSeatList = new ArrayList<>();

    @OneToMany(mappedBy = "show", cascade = CascadeType.ALL)
    @ToString.Exclude
    @Builder.Default
    @JsonIgnore
    private List<Ticket> ticketList = new ArrayList<>();

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Show show = (Show) o;
        return getShowId() != null && Objects.equals(getShowId(), show.getShowId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}