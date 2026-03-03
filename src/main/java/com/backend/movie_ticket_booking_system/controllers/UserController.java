package com.backend.movie_ticket_booking_system.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import com.backend.movie_ticket_booking_system.config.JWTService;
import com.backend.movie_ticket_booking_system.entities.User;
import com.backend.movie_ticket_booking_system.request.AuthRequest;
import com.backend.movie_ticket_booking_system.request.UserRequest;
import com.backend.movie_ticket_booking_system.services.UserService;

import jakarta.validation.Valid;
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

    @PostMapping("/addNew")
    public ResponseEntity<String> addNewUser(@Valid @RequestBody UserRequest userEntryDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.addUser(userEntryDto));
    }

    @PostMapping("/getToken")
    public ResponseEntity<String> authenticateAndGetToken(@RequestBody AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));

        if (authentication.isAuthenticated()) {
            return ResponseEntity.ok(jwtService.generateToken(authRequest.getUsername()));
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

    @GetMapping("/email/{emailId}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String emailId) {
        return ResponseEntity.ok(userService.getUserByEmail(emailId));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<String> updateUser(@PathVariable Integer userId, @Valid @RequestBody UserRequest userRequest) {
        return ResponseEntity.ok(userService.updateUser(userId, userRequest));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable Integer userId) {
        return ResponseEntity.ok(userService.deleteUser(userId));
    }

}