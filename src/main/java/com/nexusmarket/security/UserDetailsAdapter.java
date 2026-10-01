package com.nexusmarket.security;

import com.nexusmarket.domain.models.User;
import com.nexusmarket.domain.valueobjects.UserStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Adapts the {@link User} JPA entity to the Spring Security {@link UserDetails}
 * contract, so the domain model stays free of any security framework
 * dependency.
 *
 * <p>
 * Spring Security resolves the principal from this wrapper:
 * {@code getUsername()} returns the email (which is also the JWT subject) and
 * {@code getAuthorities()} exposes the role as a {@code ROLE_*} authority.</p>
 */
public class UserDetailsAdapter implements UserDetails {

    private final transient User user;

    public UserDetailsAdapter(User user) {
        this.user = user;
    }

    /**
     * Exposes the wrapped entity so controllers and services can recover the
     * domain object from the authenticated principal.
     *
     * @return the underlying {@link User}
     */
    public User getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.getStatus() == UserStatus.ACTIVE;
    }
}
