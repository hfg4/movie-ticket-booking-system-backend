package com.backend.movie_ticket_booking_system.controllers;

import com.backend.movie_ticket_booking_system.request.ForgotPasswordRequest;
import com.backend.movie_ticket_booking_system.request.ResetPasswordRequest;
import com.backend.movie_ticket_booking_system.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/password")
public class PasswordResetController {

    private final UserService userService;

    public PasswordResetController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/forgot")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            String response = userService.generatePasswordResetToken(request.getEmail());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/reset")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            String response = userService.resetPassword(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}