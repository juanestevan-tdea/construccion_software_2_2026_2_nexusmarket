package com.nexusmarket.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.nexusmarket.domain.models.User;
import com.nexusmarket.domain.ports.out.UserRepositoryPort;

import lombok.RequiredArgsConstructor;

/**
 * Loads users from MySQL for Spring Security.
 *
 * <p>
 * The "username" in this application is the user email, which is unique in the
 * {@code usuarios} table and is also the subject of every issued JWT.</p>
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepositoryPort userRepositoryPort;

    /**
     * @param email the user email sent as the JWT subject or login username
     * @return the adapted principal
     * @throws UsernameNotFoundException when no user owns that email
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepositoryPort.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        return new UserDetailsAdapter(user);
    }
}
