package com.backend.movie_ticket_booking_system.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.backend.movie_ticket_booking_system.entities.Show;
import com.backend.movie_ticket_booking_system.request.ShowRequest;
import com.backend.movie_ticket_booking_system.request.ShowSeatRequest;
import com.backend.movie_ticket_booking_system.services.ShowService;

import java.util.List;

@RestController
@RequestMapping("/show")
public class ShowController {

    @Autowired
    private ShowService showService;

    @PostMapping("/addNew")
    public ResponseEntity<String> addShow(@RequestBody ShowRequest showRequest) {
        try {
            String result = showService.addShow(showRequest);
            return new ResponseEntity<>(result, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/associateSeats")
    public ResponseEntity<String> associateShowSeats(@RequestBody ShowSeatRequest showSeatRequest) {
        try {
            String result = showService.associateShowSeats(showSeatRequest);
            return new ResponseEntity<>(result, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{showId}")
    public ResponseEntity<Show> getShowById(@PathVariable Integer showId) {
        try {
            Show show = showService.getShowById(showId);
            return new ResponseEntity<>(show, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<Show>> getAllShows() {
        try {
            List<Show> shows = showService.getAllShows();
            return new ResponseEntity<>(shows, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<List<Show>> getAllShowsOfMovie(@PathVariable Integer movieId) {
        try {
            List<Show> shows = showService.getAllShowsOfMovie(movieId);
            return new ResponseEntity<>(shows, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{showId}")
    public ResponseEntity<String> updateShow(@PathVariable Integer showId, @RequestBody ShowRequest showRequest) {
        try {
            String result = showService.updateShow(showId, showRequest);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{showId}")
    public ResponseEntity<String> deleteShow(@PathVariable Integer showId) {
        try {
            String result = showService.deleteShow(showId);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

}
