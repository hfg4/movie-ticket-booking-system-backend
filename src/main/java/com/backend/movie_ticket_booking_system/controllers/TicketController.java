package com.backend.movie_ticket_booking_system.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.backend.movie_ticket_booking_system.entities.Ticket;
import com.backend.movie_ticket_booking_system.request.TicketRequest;
import com.backend.movie_ticket_booking_system.services.TicketService;

import java.util.List;

@RestController
@RequestMapping("/ticket")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping("/addNew")
    public ResponseEntity<Object> ticketBooking(@RequestBody TicketRequest ticketRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.ticketBooking(ticketRequest)); // ✅ instance call, added semicolon
    }

    @PostMapping("/hold")
    public ResponseEntity<String> holdSeats(@RequestBody TicketRequest ticketRequest) {
        return ResponseEntity.ok(ticketService.holdSeats(ticketRequest));
    }

    @PostMapping("/release")
    public ResponseEntity<String> releaseSeats(@RequestBody TicketRequest ticketRequest) {
        return ResponseEntity.ok(ticketService.releaseSeats(ticketRequest));
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<Ticket> getTicketById(@PathVariable Integer ticketId) {
        return ResponseEntity.ok(ticketService.getTicketById(ticketId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Ticket>> getAllTickets() {
        return ResponseEntity.ok(ticketService.getAllTickets());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Ticket>> getTicketsByUserId(@PathVariable Integer userId) {
        return ResponseEntity.ok(ticketService.getTicketsByUserId(userId));
    }

    @PutMapping("/{ticketId}/rate")
    public ResponseEntity<String> rateTicket(@PathVariable Integer ticketId, @RequestParam Integer rating) {
        return ResponseEntity.ok(ticketService.rateTicket(ticketId, rating));
    }

    @DeleteMapping("/cancel/{ticketId}")
    public ResponseEntity<String> cancelTicket(@PathVariable Integer ticketId) {
        return ResponseEntity.ok(ticketService.cancelTicket(ticketId));
    }
}