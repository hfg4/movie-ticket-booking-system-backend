package com.backend.movie_ticket_booking_system.repositories;

import com.backend.movie_ticket_booking_system.entities.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Date;
import java.sql.Time;
import java.util.List;
import java.util.Optional;

public interface ShowRepository extends JpaRepository<Show, Integer> {
@SuppressWarnings("unused")
    @Query(value = "select show_time from shows where show_date = :date and movie_id = :movieId and theater_id = :theaterId and is_deleted = false", nativeQuery = true)
    List<Time> getShowTimingsOnDate(@Param("date") Date date, @Param("theaterId") Integer theaterId, @Param("movieId") Integer movieId);
@SuppressWarnings("unused")
    @Query(value = "select movie_id from shows where is_deleted = false group by movie_id order by count(*) desc limit 1" , nativeQuery = true)
    Integer getMostShowsMovie();

    @Query(value = "select * from shows where movie_id = :movieId and is_deleted = false" , nativeQuery = true)
    List<Show> getAllShowsOfMovie(@Param("movieId") Integer movieId);

    List<Show> findAllByIsDeletedFalse();

    Optional<Show> findByShowIdAndIsDeletedFalse(Integer showId);
}