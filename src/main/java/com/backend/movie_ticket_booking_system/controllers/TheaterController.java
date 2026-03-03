package com.backend.movie_ticket_booking_system.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.backend.movie_ticket_booking_system.entities.Theater;
import com.backend.movie_ticket_booking_system.request.TheaterRequest;
import com.backend.movie_ticket_booking_system.request.TheaterSeatRequest;
import com.backend.movie_ticket_booking_system.services.TheaterService;

import java.util.List;

@RestController
@RequestMapping("/theater")
public class TheaterController {

    @Autowired
    private TheaterService theaterService;

    @PostMapping("/addNew")
    public ResponseEntity<String> addTheater(@RequestBody TheaterRequest request) {
        try {
            String result = theaterService.addTheater(request);
            return new ResponseEntity<>(result, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/addTheaterSeat")
    public ResponseEntity<String> addTheaterSeat(@RequestBody TheaterSeatRequest entryDto) {
        try {
            String result = theaterService.addTheaterSeat(entryDto);
            return new ResponseEntity<>(result, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{theaterId}")
    public ResponseEntity<Theater> getTheaterById(@PathVariable Integer theaterId) {
        try {
            Theater theater = theaterService.getTheaterById(theaterId);
            return new ResponseEntity<>(theater, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<Theater>> getAllTheaters() {
        try {
            List<Theater> theaters = theaterService.getAllTheaters();
            return new ResponseEntity<>(theaters, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/address/{address}")
    public ResponseEntity<Theater> getTheaterByAddress(@PathVariable String address) {
        try {
            Theater theater = theaterService.getTheaterByAddress(address);
            return new ResponseEntity<>(theater, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{theaterId}")
    public ResponseEntity<String> updateTheater(@PathVariable Integer theaterId, @RequestBody TheaterRequest theaterRequest) {
        try {
            String result = theaterService.updateTheater(theaterId, theaterRequest);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{theaterId}")
    public ResponseEntity<String> deleteTheater(@PathVariable Integer theaterId) {
        try {
            String result = theaterService.deleteTheater(theaterId);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

}
