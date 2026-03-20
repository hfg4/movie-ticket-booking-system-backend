package com.backend.movie_ticket_booking_system.services;

import com.backend.movie_ticket_booking_system.entities.User;
import com.backend.movie_ticket_booking_system.enums.Gender;
import com.backend.movie_ticket_booking_system.enums.Role;
import com.backend.movie_ticket_booking_system.exceptions.UserDoesNotExist;
import com.backend.movie_ticket_booking_system.exceptions.UserExist;
import com.backend.movie_ticket_booking_system.repositories.UserRepository;
import com.backend.movie_ticket_booking_system.request.UserRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * KIỂM THỬ HỘP TRẮNG - UserService
 *
 * Kiểm tra các nhánh logic:
 * - addUser: email duplicate check
 * - toggleOneTap: null safe, paymentToken check
 * - toggleLock: active ↔ inactive
 * - addPaymentMethod: null/empty token handling
 * - deleteUser: user not found
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserRequest userRequest;

    @BeforeEach
    void setUp() {
        // UserService uses @Autowired field injection for passwordEncoder,
        // so @InjectMocks can't inject it via constructor. Manual injection needed.
        ReflectionTestUtils.setField(userService, "passwordEncoder", passwordEncoder);
        Set<Role> roles = new HashSet<>();
        roles.add(Role.CUSTOMER);

        testUser = User.builder()
                .id(1)
                .name("Nguyen Van A")
                .email("test@cinema.com")
                .password("encodedPassword")
                .age(25)
                .gender(Gender.MALE)
                .mobileNo("0912345678")
                .isActive(true)
                .isOneTapEnabled(false)
                .paymentToken(null)
                .roles(roles)
                .build();

        userRequest = new UserRequest();
        userRequest.setName("Nguyen Van A");
        userRequest.setEmail("test@cinema.com");
        userRequest.setPassword("password123");
        userRequest.setAge(25);
        userRequest.setGender(Gender.MALE);
        userRequest.setMobileNo("0912345678");
        userRequest.setRoles("CUSTOMER");
    }

    // =========================================================================
    // WB11: addUser() — Email chưa tồn tại → lưu thành công
    // Branch: users.isEmpty → skip if → save
    // =========================================================================
    @Test
    @DisplayName("WB11 - addUser: Email mới → đăng ký thành công")
    void addUser_WhenEmailNotExist_ShouldSaveSuccessfully() {
        // Arrange
        when(userRepository.findByEmail("test@cinema.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        String result = userService.addUser(userRequest);

        // Assert
        assertEquals("User Saved Successfully", result);
        verify(userRepository, times(1)).save(any(User.class));
    }

    // =========================================================================
    // WB12: addUser() — Email đã tồn tại → throw UserExist
    // Branch: users.isPresent → throw
    // =========================================================================
    @Test
    @DisplayName("WB12 - addUser: Email đã tồn tại → throw UserExist")
    void addUser_WhenEmailExists_ShouldThrowException() {
        // Arrange
        when(userRepository.findByEmail("test@cinema.com")).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(UserExist.class, () -> userService.addUser(userRequest));
        verify(userRepository, never()).save(any());
    }

    // =========================================================================
    // WB13: toggleOneTap() — Bật khi đã có paymentToken → thành công
    // Branch: isEnabled=false && paymentToken != null → set true
    // =========================================================================
    @Test
    @DisplayName("WB13 - toggleOneTap: Bật khi đã có paymentToken → bật thành công")
    void toggleOneTap_WhenHasPaymentToken_ShouldEnable() {
        // Arrange: user đã có paymentToken, đang tắt
        testUser.setPaymentToken("card_token_123");
        testUser.setIsOneTapEnabled(false);
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        String result = userService.toggleOneTap(1);

        // Assert
        assertTrue(testUser.getIsOneTapEnabled());
        assertEquals("Thanh toán 1 chạm đã bật", result);
    }

    // =========================================================================
    // WB14: toggleOneTap() — Bật khi chưa có paymentToken → RuntimeException
    // Branch: isEnabled=false && paymentToken == null → throw
    // =========================================================================
    @Test
    @DisplayName("WB14 - toggleOneTap: Bật khi chưa có paymentToken → throw RuntimeException")
    void toggleOneTap_WhenNoPaymentToken_ShouldThrowException() {
        // Arrange: user chưa có paymentToken
        testUser.setPaymentToken(null);
        testUser.setIsOneTapEnabled(false);
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.toggleOneTap(1));
        assertTrue(exception.getMessage().contains("liên kết phương thức thanh toán"));
    }

    // =========================================================================
    // WB15: toggleOneTap() — Tắt (đang bật → tắt)
    // Branch: isEnabled=true → set false (không check paymentToken)
    // =========================================================================
    @Test
    @DisplayName("WB15 - toggleOneTap: Đang bật → tắt thành công")
    void toggleOneTap_WhenAlreadyEnabled_ShouldDisable() {
        // Arrange
        testUser.setIsOneTapEnabled(true);
        testUser.setPaymentToken("card_token_123");
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        String result = userService.toggleOneTap(1);

        // Assert
        assertFalse(testUser.getIsOneTapEnabled());
        assertEquals("Thanh toán 1 chạm đã tắt", result);
    }

    // =========================================================================
    // WB16: toggleLock() — Khóa tài khoản (active → inactive)
    // Branch: isActive=true → set false
    // =========================================================================
    @Test
    @DisplayName("WB16 - toggleLock: Tài khoản đang mở → khóa (active → false)")
    void toggleLock_WhenActive_ShouldLock() {
        // Arrange
        testUser.setIsActive(true);
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        String result = userService.toggleLock(1);

        // Assert
        assertFalse(testUser.getIsActive());
        assertEquals("Tài khoản đã bị khóa", result);
    }

    // =========================================================================
    // WB17: toggleLock() — Mở khóa tài khoản (inactive → active)
    // Branch: isActive=false → set true
    // =========================================================================
    @Test
    @DisplayName("WB17 - toggleLock: Tài khoản đã khóa → mở khóa (inactive → true)")
    void toggleLock_WhenInactive_ShouldUnlock() {
        // Arrange
        testUser.setIsActive(false);
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        String result = userService.toggleLock(1);

        // Assert
        assertTrue(testUser.getIsActive());
        assertEquals("Tài khoản đã được mở khóa", result);
    }

    // =========================================================================
    // WB18: addPaymentMethod() — Token hợp lệ → lưu token
    // Branch: token != null && !empty → save token
    // =========================================================================
    @Test
    @DisplayName("WB18 - addPaymentMethod: Token hợp lệ → lưu phương thức thanh toán")
    void addPaymentMethod_WithValidToken_ShouldSave() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        String result = userService.addPaymentMethod(1, "card_token_456");

        // Assert
        assertEquals("card_token_456", testUser.getPaymentToken());
        assertEquals("Liên kết phương thức thanh toán thành công", result);
    }

    // =========================================================================
    // WB19: addPaymentMethod() — Token null/rỗng → gỡ token, tắt OneTap
    // Branch: token == null || empty → set null + disable OneTap
    // =========================================================================
    @Test
    @DisplayName("WB19 - addPaymentMethod: Token rỗng → gỡ phương thức, tắt OneTap")
    void addPaymentMethod_WithEmptyToken_ShouldRemoveAndDisableOneTap() {
        // Arrange
        testUser.setPaymentToken("old_token");
        testUser.setIsOneTapEnabled(true);
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        String result = userService.addPaymentMethod(1, "");

        // Assert
        assertNull(testUser.getPaymentToken());
        assertFalse(testUser.getIsOneTapEnabled());
        assertEquals("Đã gỡ phương thức thanh toán", result);
    }

    // =========================================================================
    // WB20: deleteUser() — User không tồn tại → throw UserDoesNotExist
    // Branch: userOpt.isEmpty → throw
    // =========================================================================
    @Test
    @DisplayName("WB20 - deleteUser: User không tồn tại → throw UserDoesNotExist")
    void deleteUser_WhenNotExists_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserDoesNotExist.class, () -> userService.deleteUser(999));
        verify(userRepository, never()).deleteById(any());
    }
}
