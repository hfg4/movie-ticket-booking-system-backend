package com.backend.movie_ticket_booking_system.controllers;

import com.backend.movie_ticket_booking_system.config.JWTService;
import com.backend.movie_ticket_booking_system.entities.User;
import com.backend.movie_ticket_booking_system.request.AuthRequest;
import com.backend.movie_ticket_booking_system.request.UserRequest;
import com.backend.movie_ticket_booking_system.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;

    public UserController(UserService userService, AuthenticationManager authenticationManager, JWTService jwtService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@Valid @RequestBody UserRequest userEntryDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.addUser(userEntryDto));
    }

    @PostMapping("/login")
    public ResponseEntity<String> authenticateAndGetToken(@RequestBody AuthRequest authRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword()));

            if (authentication.isAuthenticated()) {
                return ResponseEntity.ok(jwtService.generateToken(authRequest.getEmail()));
            }
        } catch (DisabledException | LockedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Tài khoản của bạn đã bị khóa, vui lòng liên hệ admin");
        } catch (Exception e) {
            throw new UsernameNotFoundException("Invalid user credentials.");
        }

        throw new UsernameNotFoundException("Invalid user credentials.");
    }

    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable Integer userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<String> updateUser(@PathVariable Integer userId, @Valid @RequestBody UserRequest userRequest) {
        return ResponseEntity.ok(userService.updateUser(userId, userRequest));
    }

    @PutMapping("/{userId}/toggleLock")
    public ResponseEntity<String> toggleLock(@PathVariable Integer userId) {
        return ResponseEntity.ok(userService.toggleLock(userId));
    }

    @PostMapping("/{userId}/toggle-one-tap")
    public ResponseEntity<String> toggleOneTap(@PathVariable Integer userId) {
        return ResponseEntity.ok(userService.toggleOneTap(userId));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable Integer userId) {
        return ResponseEntity.ok(userService.deleteUser(userId));
    }

    @PutMapping("/{userId}/password")
    public ResponseEntity<String> updatePassword(@PathVariable Integer userId, @RequestBody java.util.Map<String, String> payload) {
        String newPassword = payload.get("newPassword");
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Password cannot be empty");
        }
        return ResponseEntity.ok(userService.updateUserPassword(userId, newPassword));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody java.util.Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required");
        }
        return ResponseEntity.ok(userService.generatePasswordResetToken(email));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody java.util.Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");
        if (token == null || newPassword == null) {
            return ResponseEntity.badRequest().body("Token and newPassword are required");
        }
        return ResponseEntity.ok(userService.resetPassword(token, newPassword));
    }
    @PostMapping("/{userId}/add-payment-method")
    public ResponseEntity<String> addPaymentMethod(@PathVariable Integer userId, @RequestBody java.util.Map<String, String> payload) {
        String token = payload.get("token");
        return ResponseEntity.ok(userService.addPaymentMethod(userId, token));
    }

}