package com.backend.movie_ticket_booking_system.services;

import com.backend.movie_ticket_booking_system.convertor.UserConvertor;
import com.backend.movie_ticket_booking_system.entities.User;
import com.backend.movie_ticket_booking_system.enums.Role;
import com.backend.movie_ticket_booking_system.exceptions.UserDoesNotExist;
import com.backend.movie_ticket_booking_system.exceptions.UserExist;
import com.backend.movie_ticket_booking_system.repositories.UserRepository;
import com.backend.movie_ticket_booking_system.request.UserRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import com.backend.movie_ticket_booking_system.entities.Movie;
import com.backend.movie_ticket_booking_system.repositories.MovieRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.backend.movie_ticket_booking_system.repositories.PasswordResetTokenRepository tokenRepository;
    private final org.springframework.mail.javamail.JavaMailSender mailSender;

    public UserService(UserRepository userRepository, 
                       MovieRepository movieRepository,
                       PasswordEncoder passwordEncoder, 
                       com.backend.movie_ticket_booking_system.repositories.PasswordResetTokenRepository tokenRepository, 
                       org.springframework.mail.javamail.JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.movieRepository = movieRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenRepository = tokenRepository;
        this.mailSender = mailSender;
    }

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
        if (userRequest.getDateOfBirth() != null) {
            user.setDateOfBirth(userRequest.getDateOfBirth());
            user.setAge(java.time.Period.between(userRequest.getDateOfBirth(), java.time.LocalDate.now()).getYears());
        } else if (userRequest.getAge() != null) {
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
        if (userRequest.getIsOneTapEnabled() != null) {
            user.setIsOneTapEnabled(userRequest.getIsOneTapEnabled());
        } else if (user.getIsOneTapEnabled() == null) {
            user.setIsOneTapEnabled(false);
        }
        if (userRequest.getPaymentToken() != null) {
            user.setPaymentToken(userRequest.getPaymentToken());
        }

        userRepository.save(user);
        return "User updated successfully";
    }

    public String toggleOneTap(Integer userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new UserDoesNotExist();
        }
        User user = userOpt.get();
        
        // Handle NULL safely to prevent NullPointerException on unboxing
        boolean isEnabled = Boolean.TRUE.equals(user.getIsOneTapEnabled());
        
        if (!isEnabled && (user.getPaymentToken() == null || user.getPaymentToken().isEmpty())) {
            throw new RuntimeException("Bạn cần liên kết phương thức thanh toán trước khi bật tính năng này!");
        }

        user.setIsOneTapEnabled(!isEnabled);
        userRepository.save(user);
        return user.getIsOneTapEnabled() ? "Thanh toán 1 chạm đã bật" : "Thanh toán 1 chạm đã tắt";
    }

    public String addPaymentMethod(Integer userId, String token) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new UserDoesNotExist();
        }
        User user = userOpt.get();
        if (token == null || token.trim().isEmpty()) {
            user.setPaymentToken(null);
            user.setIsOneTapEnabled(false); // Force disable if no card
            userRepository.save(user);
            return "Đã gỡ phương thức thanh toán";
        }
        user.setPaymentToken(token);
        userRepository.save(user);
        return "Liên kết phương thức thanh toán thành công";
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
@SuppressWarnings("unused")
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public String toggleFavoriteMovie(Integer userId, Integer movieId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new UserDoesNotExist();
        }
        User user = userOpt.get();

        Optional<Movie> movieOpt = movieRepository.findById(movieId);
        if (movieOpt.isEmpty()) {
            throw new RuntimeException("Phim không tồn tại");
        }
        Movie movie = movieOpt.get();

        List<Movie> favorites = user.getFavoriteMovies();
        boolean removed = favorites.removeIf(m -> m.getId().equals(movieId));
        
        if (removed) {
            userRepository.save(user);
            return "Đã xóa khỏi phim yêu thích";
        } else {
            favorites.add(movie);
            userRepository.save(user);
            return "Đã thêm vào phim yêu thích";
        }
    }
}
