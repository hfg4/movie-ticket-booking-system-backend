package com.backend.movie_ticket_booking_system.response;

import com.backend.movie_ticket_booking_system.enums.Gender;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {

    private String name;
    private Integer age;
    private Gender gender;
    private String address;
    private String email;
    private String mobileNo;
}
