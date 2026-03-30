package com.backend.movie_ticket_booking_system.convertor;

import com.backend.movie_ticket_booking_system.entities.User;
import com.backend.movie_ticket_booking_system.enums.Role;
import com.backend.movie_ticket_booking_system.request.UserRequest;
import com.backend.movie_ticket_booking_system.response.UserResponse;

import java.util.Arrays;
import java.util.stream.Collectors;

public class UserConvertor {

    public static User userDtoToUser(UserRequest userRequest, String password) {

        Integer computedAge = userRequest.getAge();
        if (userRequest.getDateOfBirth() != null) {
            computedAge = java.time.Period.between(userRequest.getDateOfBirth(), java.time.LocalDate.now()).getYears();
        }

        return User.builder()
                .name(userRequest.getName())
                .dateOfBirth(userRequest.getDateOfBirth())
                .age(computedAge)
                .address(userRequest.getAddress())
                .gender(userRequest.getGender())
                .mobileNo(userRequest.getMobileNo())
                .email(userRequest.getEmail())
                .roles(Arrays.stream(userRequest.getRoles().split(","))
                        .map(String::trim)
                        .map(Role::valueOf)
                        .collect(Collectors.toSet()))
                .password(password)
                .build();
    }
@SuppressWarnings("unused")
    public static UserResponse userToUserDto(User user) {

        return UserResponse.builder()
                .name(user.getName())
                .age(user.getAge())
                .address(user.getAddress())
                .gender(user.getGender())
                .email(user.getEmail())
                .mobileNo(user.getMobileNo())
                .build();
    }
}