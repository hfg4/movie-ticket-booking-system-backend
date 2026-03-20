package com.backend.movie_ticket_booking_system.config;

import com.backend.movie_ticket_booking_system.entities.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class UserInfoUserDetails implements UserDetails {

    @Serial
    private static final long serialVersionUID = -8773921465190832995L;
    private final String name;
    private final String password;
    private final boolean isActive;
    private final List<GrantedAuthority> authorities;

    public UserInfoUserDetails(User userInfo) {
        name = userInfo.getEmail();
        password = userInfo.getPassword();
        isActive = userInfo.getIsActive() != null ? userInfo.getIsActive() : true;
        authorities = userInfo.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return name;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return isActive;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }

}
