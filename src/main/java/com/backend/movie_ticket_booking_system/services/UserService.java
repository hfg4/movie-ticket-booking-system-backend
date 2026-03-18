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

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private com.backend.movie_ticket_booking_system.repositories.PasswordResetTokenRepository tokenRepository;

    @Autowired
    private org.springframework.mail.javamail.JavaMailSender mailSender;

    public String addUser(UserRequest userRequest) {
        Optional<User> users = userRepository.findByEmail(userRequest.getEmail());

        if (users.isPresent()) {
            throw new UserExist();
        }

        User user = UserConvertor.userDtoToUser(userRequest,  passwordEncoder.encode(userRequest.getPassword()));

        userRepository.save(user);
        return "User Saved Successfully";
    }

    public User getUserById(Integer userId) {
        Optional<User> userOpt = userRepository.findByIdAndIsActiveTrue(userId);

        if (userOpt.isEmpty()) {
            throw new UserDoesNotExist();
        }

        return userOpt.get();
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserByEmail(String emailId) {
        Optional<User> userOpt = userRepository.findByEmailAndIsActiveTrue(emailId);

        if (userOpt.isEmpty()) {
            throw new UserDoesNotExist();
        }

        return userOpt.get();
    }

    public String updateUser(Integer userId, UserRequest userRequest) {
        Optional<User> userOpt = userRepository.findByIdAndIsActiveTrue(userId);

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
        if (userRequest.getEmail() != null) {
            Optional<User> existingUser = userRepository.findByEmail(userRequest.getEmail());
            if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
                throw new UserExist();
            }
            user.setEmail(userRequest.getEmail());
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

        try {
            userRepository.deleteById(userId);
            return "User deleted successfully";
        } catch (Exception e) {
            return "Không thể xóa người dùng do có dữ liệu liên quan (ví dụ: vé đã đặt).";
        }
    }

    public String toggleLock(Integer userId) {
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isEmpty()) {
            throw new UserDoesNotExist();
        }

        User user = userOpt.get();
        user.setIsActive(!user.getIsActive());
        userRepository.save(user);
        return user.getIsActive() ? "Tài khoản đã được mở khóa" : "Tài khoản đã bị khóa";
    }

    public String updateUserPassword(Integer userId, String newPassword) {
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isEmpty()) {
            throw new UserDoesNotExist();
        }

        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return "User password updated successfully";
    }

    @org.springframework.transaction.annotation.Transactional
    public String generatePasswordResetToken(String email) {
        System.out.println("Processing forgot password for email: " + email);
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Email không tồn tại trong hệ thống");
        }
        User user = userOpt.get();

        // Delete any existing tokens for this user
        tokenRepository.deleteByUser(user);

        // Generate a 6-digit OTP
        String otp = String.format("%06d", new java.util.Random().nextInt(999999));

        com.backend.movie_ticket_booking_system.entities.PasswordResetToken token = 
            com.backend.movie_ticket_booking_system.entities.PasswordResetToken.builder()
                .token(otp)
                .user(user)
                .expiryDate(java.time.LocalDateTime.now().plusMinutes(10))
                .build();
                
        tokenRepository.save(token);

        // Send Email
        org.springframework.mail.SimpleMailMessage message = new org.springframework.mail.SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Mã xác minh đặt lại mật khẩu - Cinema");
        message.setText("Xin chào " + user.getName() + ",\n\n"
                + "Bạn đã yêu cầu đặt lại mật khẩu. Mã OTP của bạn là: " + otp + "\n\n"
                + "Mã này sẽ hết hạn sau 10 phút. Vui lòng không chia sẻ mã này cho bất kỳ ai.\n\n"
                + "Trân trọng,\nĐội ngũ Cinema");
        
        try {
            mailSender.send(message);
        } catch(Exception e) {
             throw new RuntimeException("Không thể gửi email: " + e.getMessage());
        }

        return "Mã OTP đã được gửi đến email của bạn";
    }

    @org.springframework.transaction.annotation.Transactional
    public String resetPassword(String token, String newPassword) {
        Optional<com.backend.movie_ticket_booking_system.entities.PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);
        if (tokenOpt.isEmpty()) {
            throw new RuntimeException("Mã OTP không hợp lệ hoặc đã hết hạn.");
        }

        com.backend.movie_ticket_booking_system.entities.PasswordResetToken resetToken = tokenOpt.get();
        if (resetToken.isExpired()) {
            tokenRepository.delete(resetToken);
            throw new RuntimeException("Mã OTP đã hết hạn.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        tokenRepository.delete(resetToken);

        return "Đổi mật khẩu thành công!";
    }

}
