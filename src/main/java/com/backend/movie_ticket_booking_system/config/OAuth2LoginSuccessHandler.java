package com.backend.movie_ticket_booking_system.config;

import com.backend.movie_ticket_booking_system.entities.User;
import com.backend.movie_ticket_booking_system.enums.Role;
import com.backend.movie_ticket_booking_system.repositories.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTService jwtService;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        Optional<User> userOpt = userRepository.findByEmail(email);
        User user;

        if (userOpt.isEmpty()) {
            // Create a new user for Google login
            user = User.builder()
                    .email(email)
                    .name(name)
                    .password("") // Google users don't have a local password initially
                    .roles(Set.of(Role.CUSTOMER))
                    .isActive(true)
                    .build();
            userRepository.save(user);
        } else {
            user = userOpt.get();
        }

        String token = jwtService.generateToken(email);
        
        // Redirect to frontend with token
        // In a real app, you might use a more secure way to pass the token
        String targetUrl = "http://localhost:4200/auth?token=" + token;
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
