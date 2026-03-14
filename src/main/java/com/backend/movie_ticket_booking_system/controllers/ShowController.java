package com.backend.movie_ticket_booking_system.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.backend.movie_ticket_booking_system.entities.Show;
import com.backend.movie_ticket_booking_system.entities.ShowSeat;
import com.backend.movie_ticket_booking_system.request.ShowRequest;
import com.backend.movie_ticket_booking_system.request.ShowSeatRequest;
import com.backend.movie_ticket_booking_system.services.ShowService;

import java.util.List;

@RestController
@RequestMapping("/show")
public class ShowController {

    private final ShowService showService;

    public ShowController(ShowService showService) {
        this.showService = showService;
    }

    @PostMapping("/addNew")
    public ResponseEntity<String> addShow(@RequestBody ShowRequest showRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(showService.addShow(showRequest));
    }

    @PostMapping("/associateSeats")
    public ResponseEntity<String> associateShowSeats(@RequestBody ShowSeatRequest showSeatRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(showService.associateShowSeats(showSeatRequest));
    }

    @GetMapping("/{showId}")
    public ResponseEntity<Show> getShowById(@PathVariable Integer showId) {
        return ResponseEntity.ok(showService.getShowById(showId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Show>> getAllShows() {
        return ResponseEntity.ok(showService.getAllShows());
    }

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<List<Show>> getAllShowsOfMovie(@PathVariable Integer movieId) {
        return ResponseEntity.ok(showService.getAllShowsOfMovie(movieId));
    }

    @PutMapping("/{showId}")
    public ResponseEntity<String> updateShow(@PathVariable Integer showId, @RequestBody ShowRequest showRequest) {
        return ResponseEntity.ok(showService.updateShow(showId, showRequest));
    }

    @DeleteMapping("/{showId}")
    public ResponseEntity<String> deleteShow(@PathVariable Integer showId) {
        return ResponseEntity.ok(showService.deleteShow(showId));
    }

    @PutMapping("/{showId}/seats")
    public ResponseEntity<List<ShowSeat>> updateShowSeats(@PathVariable Integer showId, @RequestBody List<ShowSeat> updatedSeats) {
        showService.updateShowSeats(showId, updatedSeats);
        return ResponseEntity.ok(showService.getShowById(showId).getShowSeatList());
    }
}