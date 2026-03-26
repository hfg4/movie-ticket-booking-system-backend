package com.backend.movie_ticket_booking_system.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final JwtAuthFilter authFilter;
    private final UserInfoUserDetailsService userDetailsService;
    private final OAuth2LoginSuccessHandler oauth2LoginSuccessHandler;

    @Value("${cors.allowed-origins:http://localhost:4200}")
    private String corsAllowedOrigins;

    @Value("${cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS,PATCH}")
    private String corsAllowedMethods;

    @Value("${cors.max-age:3600}")
    private long corsMaxAge;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("Configuring security filter chain");

        return http
                // CORS Configuration
                .cors(c -> c.configurationSource(corsConfigurationSource()))

                // CSRF Protection - Disabled for stateless JWT API
                .csrf(AbstractHttpConfigurer::disable)

                // Authorization Rules
                .authorizeHttpRequests(req -> req
                        // Public endpoints - Swagger/API documentation
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/swagger-ui/index.html",
                                "/actuator/health"
                        ).permitAll()

                        // Public endpoints - User registration and login
                        .requestMatchers(
                                "/user/register",
                                "/user/login",
                                "/user/refresh-token",
                                "/user/forgot-password",
                                "/user/reset-password",
                                "/login/oauth2/**"
                        ).permitAll()
                        
                        // Admin Dashboard endpoint
                        .requestMatchers("/admin/dashboard/**").hasAuthority("ROLE_ADMIN")

                        // Public endpoints - Read-only operations
                        .requestMatchers(
                                "/movie/all",
                                "/movie/name/**",
                                "/movie/actor/**",
                                "/theater/all",
                                "/show/all",
                                "/show/movie/**",
                                "/movie/*",
                                "/show/*",
                                "/theater/*",
                                "/upload/**"
                        ).permitAll()

                        // User endpoints - Accessible to all authenticated users
                        .requestMatchers("/user/**").hasAnyAuthority("ROLE_CUSTOMER", "ROLE_ADMIN")
                        .requestMatchers("/movie/**").hasAuthority("ROLE_ADMIN")

                        // Admin-only endpoints - Show management
                        .requestMatchers("/show/**").hasAuthority("ROLE_ADMIN")

                        // Admin-only endpoints - Theater management
                        .requestMatchers("/theater/**").hasAuthority("ROLE_ADMIN")

                        // Ticket endpoints - Accessible to both users and admins
                        .requestMatchers("/ticket/**").hasAnyAuthority("ROLE_CUSTOMER", "ROLE_ADMIN")

                        // All other requests require authentication
                        .anyRequest().authenticated()
                )

                // Session Management - Stateless (JWT)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Authentication Provider
                .authenticationProvider(authenticationProvider())

                // JWT Filter - Before UsernamePasswordAuthenticationFilter
                .addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class)

                // OAuth2 Login
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oauth2LoginSuccessHandler)
                )

                // Exception Handling for API
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(unauthorizedEntryPoint())
                )

                .build();
    }

    @Bean
    public AuthenticationEntryPoint unauthorizedEntryPoint() {
        return (request, response, authException) -> {
            log.warn("Unauthorized access attempt: {}", authException.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"" + authException.getMessage() + "\"}");
        };
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        log.info("Configuring CORS with origins: {}", corsAllowedOrigins);

        CorsConfiguration configuration = new CorsConfiguration();

        // Parse multiple origins separated by comma
        List<String> origins = Arrays.stream(corsAllowedOrigins.split(","))
                .map(String::trim)
                .toList();
        configuration.setAllowedOrigins(origins);

        // Allowed HTTP methods
        List<String> methods = Arrays.stream(corsAllowedMethods.split(","))
                .map(String::trim)
                .toList();
        configuration.setAllowedMethods(methods);

        // Allow all headers
        configuration.setAllowedHeaders(List.of("*"));

        // Expose specific headers
        configuration.setExposedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "X-Total-Count"  // Useful for pagination
        ));

        // Allow credentials (cookies, authorization header)
        configuration.setAllowCredentials(true);

        // Cache preflight request
        configuration.setMaxAge(corsMaxAge);

        // Register configuration for all paths
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    AuthenticationProvider authenticationProvider() {
        log.debug("Setting up authentication provider");

        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        provider.setHideUserNotFoundExceptions(false);  // Show user not found errors

        return provider;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);  // Strength 12 = more secure
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
