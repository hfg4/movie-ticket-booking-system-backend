package com.backend.movie_ticket_booking_system.services;

import com.backend.movie_ticket_booking_system.convertor.UserConvertor;
import com.backend.movie_ticket_booking_system.entities.User;
import com.backend.movie_ticket_booking_system.enums.Role;
import com.backend.movie_ticket_booking_system.exceptions.UserDoesNotExist;
import com.backend.movie_ticket_booking_system.exceptions.UserExist;
import com.backend.movie_ticket_booking_system.repositories.UserRepository;
import com.backend.movie_ticket_booking_system.request.UserRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public String addUser(UserRequest userRequest) {
        Optional<User> users = userRepository.findByEmailId(userRequest.getEmailId());

        if (users.isPresent()) {
            throw new UserExist();
        }

        User user = UserConvertor.userDtoToUser(userRequest,  passwordEncoder.encode(userRequest.getPassword()));

        userRepository.save(user);
        return "User Saved Successfully";
    }

    public User getUserById(Integer userId) {
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isEmpty()) {
            throw new UserDoesNotExist();
        }

        return userOpt.get();
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserByEmail(String emailId) {
        Optional<User> userOpt = userRepository.findByEmailId(emailId);

        if (userOpt.isEmpty()) {
            throw new UserDoesNotExist();
        }

        return userOpt.get();
    }

    public String updateUser(Integer userId, UserRequest userRequest) {
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isEmpty()) {
            throw new UserDoesNotExist();
        }

        User user = userOpt.get();

        if (userRequest.getName() != null) {
            user.setName(userRequest.getName());
        }
        if (userRequest.getAge() != null) {
            user.setAge(userRequest.getAge());
        }
        if (userRequest.getAddress() != null) {
            user.setAddress(userRequest.getAddress());
        }
        if (userRequest.getGender() != null) {
            user.setGender(userRequest.getGender());
        }
        if (userRequest.getMobileNo() != null) {
            user.setMobileNo(userRequest.getMobileNo());
        }
        if (userRequest.getEmailId() != null) {
            Optional<User> existingUser = userRepository.findByEmailId(userRequest.getEmailId());
            if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
                throw new UserExist();
            }
            user.setEmailId(userRequest.getEmailId());
        }
        if (userRequest.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        }
        if (userRequest.getRoles() != null) {
            user.setRoles(Arrays.stream(userRequest.getRoles().split(","))
                    .map(String::trim)
                    .map(Role::valueOf)
                    .collect(Collectors.toSet()));
        }

        userRepository.save(user);
        return "User updated successfully";
    }

    public String deleteUser(Integer userId) {
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isEmpty()) {
            throw new UserDoesNotExist();
        }

        userRepository.deleteById(userId);
        return "User deleted successfully";
    }

}
