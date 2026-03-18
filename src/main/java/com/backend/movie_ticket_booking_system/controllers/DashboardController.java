package com.backend.movie_ticket_booking_system.controllers;

import com.backend.movie_ticket_booking_system.services.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(dashboardService.getDashboardStats());
    }

    @GetMapping("/revenue-by-day")
    public ResponseEntity<List<Map<String, Object>>> getRevenueByDay() {
        return ResponseEntity.ok(dashboardService.getRevenueByDay());
    }

    @GetMapping("/top-movies")
    public ResponseEntity<List<Map<String, Object>>> getTopMovies() {
        return ResponseEntity.ok(dashboardService.getTopGrossingMovies());
    }

    @GetMapping("/revenue-by-theater")
    public ResponseEntity<List<Map<String, Object>>> getRevenueByTheater() {
        return ResponseEntity.ok(dashboardService.getRevenueByTheater());
    }
}
