package com.nexusmarket.auth.controller;

import com.nexusmarket.auth.dto.AuthResponse;
import com.nexusmarket.auth.dto.LoginRequest;
import com.nexusmarket.auth.dto.RegisterRequest;
import com.nexusmarket.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public authentication endpoints.
 *
 * <p>
 * Both routes are listed as {@code permitAll} in {@code SecurityConfig}: they
 * are the only way to obtain a JWT, so they must be reachable without one.</p>
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Self-registration for BUYER and SELLER accounts.
     *
     * @return 201 with the created profile and an initial token
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Exchanges email and password for a JWT.
     *
     * @return 200 with the token and basic profile data
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
