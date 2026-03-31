package com.backend.movie_ticket_booking_system.services;

import com.backend.movie_ticket_booking_system.repositories.CouponRepository;
import com.backend.movie_ticket_booking_system.repositories.MovieRepository;
import com.backend.movie_ticket_booking_system.repositories.ShowRepository;
import com.backend.movie_ticket_booking_system.repositories.TicketRepository;
import com.backend.movie_ticket_booking_system.repositories.UserRepository;
import com.backend.movie_ticket_booking_system.entities.Coupon;
import com.backend.movie_ticket_booking_system.entities.Show;
import com.backend.movie_ticket_booking_system.entities.Ticket;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {


    private final MovieRepository movieRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final CouponRepository couponRepository;
    private final ShowRepository showRepository;
    private final com.backend.movie_ticket_booking_system.repositories.NotificationReadRepository notificationReadRepository;

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
                .mapToDouble(Ticket::getTotalTicketsPrice)
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
                    revenueMap.put(date, revenueMap.get(date) + t.getTotalTicketsPrice());
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
                movieRevenue.put(name, movieRevenue.getOrDefault(name, 0.0) + t.getTotalTicketsPrice());
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
                theaterRevenue.put(name, theaterRevenue.getOrDefault(name, 0.0) + t.getTotalTicketsPrice());
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

    public List<Map<String, Object>> getNotifications(Integer userId) {
        List<Map<String, Object>> notifications = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        Date utilDateNow = new Date();

        // Get read notification IDs for this user
        List<String> readNotifIds = notificationReadRepository.findByUserId(userId)
                .stream().map(com.backend.movie_ticket_booking_system.entities.NotificationRead::getNotificationId).toList();

        // 1. Check expired coupons
        List<Coupon> coupons = couponRepository.findAll();
        for (Coupon coupon : coupons) {
            if (coupon.getIsActive() && coupon.getExpiresAt() != null && coupon.getExpiresAt().before(utilDateNow)) {
                String id = "c_exp_" + coupon.getId();
                Map<String, Object> notif = new HashMap<>();
                notif.put("id", id);
                notif.put("type", "COUPON_EXPIRED");
                notif.put("icon", "fa-solid fa-ticket");
                notif.put("color", "#ef4444");
                notif.put("message", "Mã giảm giá " + coupon.getCode() + " đã hết hạn.");
                notif.put("timestamp", coupon.getExpiresAt().getTime());
                notif.put("read", readNotifIds.contains(id));
                notifications.add(notif);
            }
            // 2. Check out of uses coupons
            if (coupon.getIsActive() && coupon.getMaxUses() != null && coupon.getUsedCount() != null && coupon.getUsedCount() >= coupon.getMaxUses()) {
                String id = "c_uses_" + coupon.getId();
                Map<String, Object> notif = new HashMap<>();
                notif.put("id", id);
                notif.put("type", "COUPON_EXHAUSTED");
                notif.put("icon", "fa-solid fa-users-slash");
                notif.put("color", "#f59e0b");
                notif.put("message", "Mã giảm giá " + coupon.getCode() + " đã đạt giới hạn lượt dùng (" + coupon.getMaxUses() + ").");
                notif.put("timestamp", System.currentTimeMillis() - 1000); 
                notif.put("read", readNotifIds.contains(id));
                notifications.add(notif);
            }
        }

        // 3. Check overdue active shows
        List<Show> shows = showRepository.findAll();
        for (Show show : shows) {
            if (show.getShowDate() != null && show.getShowTime() != null && show.getStatus().name().equals("ACTIVE")) {
                LocalDateTime showTime = LocalDateTime.of(show.getShowDate().toLocalDate(), show.getShowTime().toLocalTime());
                if (showTime.isBefore(now)) {
                    String id = "s_over_" + show.getShowId();
                    Map<String, Object> notif = new HashMap<>();
                    notif.put("id", id);
                    notif.put("type", "SHOW_OVERDUE");
                    notif.put("icon", "fa-solid fa-clock-rotate-left");
                    notif.put("color", "#8b5cf6");
                    String movieName = show.getMovie() != null ? show.getMovie().getMovieName() : "Không rõ";
                    notif.put("message", "Suất chiếu phim " + movieName + " lúc " + show.getShowTime() + " ngày " + show.getShowDate() + " đã trôi qua.");
                    notif.put("timestamp", java.sql.Timestamp.valueOf(showTime).getTime());
                    notif.put("read", readNotifIds.contains(id));
                    notifications.add(notif);
                }
            }
        }

        // Sort by timestamp descending
        notifications.sort((n1, n2) -> Long.compare((Long) n2.get("timestamp"), (Long) n1.get("timestamp")));

        // Return top 20 notifications
        return notifications.stream().limit(20).toList();
    }

    public void markRead(Integer userId, String notificationId) {
        if (!notificationReadRepository.existsByUserIdAndNotificationId(userId, notificationId)) {
            notificationReadRepository.save(com.backend.movie_ticket_booking_system.entities.NotificationRead.builder()
                    .userId(userId)
                    .notificationId(notificationId)
                    .build());
        }
    }

    public void markAllRead(Integer userId) {
        List<Map<String, Object>> currentNotifs = getNotifications(userId);
        for (Map<String, Object> notif : currentNotifs) {
            String id = (String) notif.get("id");
            if (!notificationReadRepository.existsByUserIdAndNotificationId(userId, id)) {
                notificationReadRepository.save(com.backend.movie_ticket_booking_system.entities.NotificationRead.builder()
                        .userId(userId)
                        .notificationId(id)
                        .build());
            }
        }
    }
}
