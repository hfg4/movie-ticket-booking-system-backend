package com.backend.movie_ticket_booking_system.controllers;

import com.backend.movie_ticket_booking_system.services.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/dashboard")
public class DashboardController {


    private final DashboardService dashboardService;

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

    @GetMapping("/notifications")
    public ResponseEntity<List<Map<String, Object>>> getNotifications(@org.springframework.security.core.annotation.AuthenticationPrincipal com.backend.movie_ticket_booking_system.config.UserInfoUserDetails userDetails) {
        if (userDetails == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(dashboardService.getNotifications(userDetails.getUserId()));
    }

    @org.springframework.web.bind.annotation.PostMapping("/mark-read/{notificationId}")
    public ResponseEntity<Void> markRead(@org.springframework.security.core.annotation.AuthenticationPrincipal com.backend.movie_ticket_booking_system.config.UserInfoUserDetails userDetails, @org.springframework.web.bind.annotation.PathVariable String notificationId) {
        if (userDetails == null) return ResponseEntity.status(401).build();
        dashboardService.markRead(userDetails.getUserId(), notificationId);
        return ResponseEntity.ok().build();
    }

    @org.springframework.web.bind.annotation.PostMapping("/mark-all-read")
    public ResponseEntity<Void> markAllRead(@org.springframework.security.core.annotation.AuthenticationPrincipal com.backend.movie_ticket_booking_system.config.UserInfoUserDetails userDetails) {
        if (userDetails == null) return ResponseEntity.status(401).build();
        dashboardService.markAllRead(userDetails.getUserId());
        return ResponseEntity.ok().build();
    }
}
