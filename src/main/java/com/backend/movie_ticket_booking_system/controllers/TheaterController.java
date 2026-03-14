package com.backend.movie_ticket_booking_system.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.backend.movie_ticket_booking_system.entities.Theater;
import com.backend.movie_ticket_booking_system.entities.TheaterSeat;
import com.backend.movie_ticket_booking_system.request.TheaterRequest;
import com.backend.movie_ticket_booking_system.request.TheaterSeatRequest;
import com.backend.movie_ticket_booking_system.services.TheaterService;

import java.util.List;

@RestController
@RequestMapping("/theater")
public class TheaterController {

    private final TheaterService theaterService;

    public TheaterController(TheaterService theaterService) {
        this.theaterService = theaterService;
    }

    @PostMapping("/addNew")
    public ResponseEntity<String> addTheater(@RequestBody TheaterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(theaterService.addTheater(request));
    }

    @PostMapping("/addTheaterSeat")
    public ResponseEntity<String> addTheaterSeat(@RequestBody TheaterSeatRequest entryDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(theaterService.addTheaterSeat(entryDto));
    }

    @GetMapping("/{theaterId}")
    public ResponseEntity<Theater> getTheaterById(@PathVariable Integer theaterId) {
        return ResponseEntity.ok(theaterService.getTheaterById(theaterId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Theater>> getAllTheaters() {
        return ResponseEntity.ok(theaterService.getAllTheaters());
    }

    @GetMapping("/address/{address}")
    public ResponseEntity<Theater> getTheaterByAddress(@PathVariable String address) {
        return ResponseEntity.ok(theaterService.getTheaterByAddress(address));
    }

    @PutMapping("/{theaterId}")
    public ResponseEntity<String> updateTheater(@PathVariable Integer theaterId, @RequestBody TheaterRequest theaterRequest) {
        return ResponseEntity.ok(theaterService.updateTheater(theaterId, theaterRequest));
    }

    @DeleteMapping("/{theaterId}")
    public ResponseEntity<String> deleteTheater(@PathVariable Integer theaterId) {
        return ResponseEntity.ok(theaterService.deleteTheater(theaterId));
    }

    @PutMapping("/{theaterId}/seats")
    public ResponseEntity<List<TheaterSeat>> updateTheaterSeats(@PathVariable Integer theaterId, @RequestBody List<TheaterSeat> updatedSeats) {
        theaterService.updateTheaterSeats(theaterId, updatedSeats);
        return ResponseEntity.ok(theaterService.getTheaterById(theaterId).getTheaterSeatList());
    }
}