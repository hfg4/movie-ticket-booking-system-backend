package com.backend.movie_ticket_booking_system.request;

import com.backend.movie_ticket_booking_system.enums.Gender;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UserRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @Min(value = 1, message = "Age must be at least 1")
    @Max(value = 150, message = "Age must be less than 150")
    private Integer age;

    private java.time.LocalDate dateOfBirth;

    private String address;

    @Pattern(regexp = "^[0-9]{10}$", message = "Mobile number must be 10 digits")
    private String mobileNo;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    private Gender gender;

    @NotBlank(message = "Role is required")
    private String roles;

    private Boolean isOneTapEnabled;

    private String paymentToken;

    private String userImage;
}