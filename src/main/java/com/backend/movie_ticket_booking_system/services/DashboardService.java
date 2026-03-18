package com.backend.movie_ticket_booking_system.services;

import com.backend.movie_ticket_booking_system.repositories.MovieRepository;
import com.backend.movie_ticket_booking_system.repositories.TicketRepository;
import com.backend.movie_ticket_booking_system.repositories.UserRepository;
import com.backend.movie_ticket_booking_system.entities.Ticket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        
        List<Ticket> allTickets = ticketRepository.findAll();
        
        // 1. Total Tickets Today
        long ticketsToday = allTickets.stream()
                .filter(t -> t.getBookedAt() != null && t.getBookedAt().toLocalDateTime().isAfter(startOfDay) && t.getBookedAt().toLocalDateTime().isBefore(endOfDay))
                .count();
        stats.put("ticketsToday", ticketsToday);

        // 2. Active Movies
        long activeMovies = movieRepository.findAllByIsDeletedFalse().size();
        stats.put("activeMovies", activeMovies);

        // 3. Revenue Today
        double revenueToday = allTickets.stream()
                .filter(t -> t.getBookedAt() != null && t.getBookedAt().toLocalDateTime().isAfter(startOfDay) && t.getBookedAt().toLocalDateTime().isBefore(endOfDay))
                .mapToDouble(t -> t.getTotalTicketsPrice().doubleValue())
                .sum();
        stats.put("revenueToday", revenueToday);

        // 4. Total Users
        long totalUsers = userRepository.count();
        stats.put("totalUsers", totalUsers);

        return stats;
    }

    public List<Map<String, Object>> getRevenueByDay() {
        List<Ticket> allTickets = ticketRepository.findAll();
        Map<LocalDate, Double> revenueMap = new HashMap<>();
        
        for (int i = 6; i >= 0; i--) {
            revenueMap.put(LocalDate.now().minusDays(i), 0.0);
        }

        allTickets.forEach(t -> {
            if (t.getBookedAt() != null) {
                LocalDate date = t.getBookedAt().toLocalDateTime().toLocalDate();
                if (revenueMap.containsKey(date)) {
                    revenueMap.put(date, revenueMap.get(date) + t.getTotalTicketsPrice().doubleValue());
                }
            }
        });

        return revenueMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("date", e.getKey().toString());
                    item.put("revenue", e.getValue());
                    return item;
                }).toList();
    }

    public List<Map<String, Object>> getTopGrossingMovies() {
        List<Ticket> allTickets = ticketRepository.findAll();
        Map<String, Double> movieRevenue = new HashMap<>();
        Map<String, Long> movieViews = new HashMap<>();

        allTickets.forEach(t -> {
            if (t.getShow() != null && t.getShow().getMovie() != null) {
                String name = t.getShow().getMovie().getMovieName();
                movieRevenue.put(name, movieRevenue.getOrDefault(name, 0.0) + t.getTotalTicketsPrice().doubleValue());
                movieViews.put(name, movieViews.getOrDefault(name, 0L) + 1);
            }
        });

        return movieRevenue.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(5)
                .map(e -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("movieName", e.getKey());
                    item.put("revenue", e.getValue());
                    item.put("views", movieViews.get(e.getKey()));
                    return item;
                }).toList();
    }

    public List<Map<String, Object>> getRevenueByTheater() {
        List<Ticket> allTickets = ticketRepository.findAll();
        Map<String, Double> theaterRevenue = new HashMap<>();

        allTickets.forEach(t -> {
            if (t.getShow() != null && t.getShow().getTheater() != null) {
                String name = t.getShow().getTheater().getName();
                theaterRevenue.put(name, theaterRevenue.getOrDefault(name, 0.0) + t.getTotalTicketsPrice().doubleValue());
            }
        });

        return theaterRevenue.entrySet().stream()
                .map(e -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("theaterName", e.getKey());
                    item.put("revenue", e.getValue());
                    return item;
                }).toList();
    }
}
