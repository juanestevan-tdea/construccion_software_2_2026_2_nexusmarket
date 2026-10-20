package com.nexusmarket.adapters.rest.controllers;

import com.nexusmarket.adapters.rest.dtos.requests.ChangePasswordRequestDTO;
import com.nexusmarket.adapters.rest.dtos.requests.LoginRequestDTO;
import com.nexusmarket.adapters.rest.dtos.requests.RegisterRequestDTO;
import com.nexusmarket.adapters.rest.dtos.responses.AuthResponseDTO;
import com.nexusmarket.domain.ports.in.AuthenticationUseCasePort;
import com.nexusmarket.domain.ports.in.model.AuthenticationResult;
import com.nexusmarket.domain.ports.in.model.LoginCommand;
import com.nexusmarket.domain.ports.in.model.RegisterCommand;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nexusmarket.infrastructure.security.UserDetailsAdapter;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationUseCasePort authenticationUseCasePort;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        RegisterCommand command = RegisterCommand.builder()
                .email(request.getEmail())
                .fullName(request.getFullName())
                .password(request.getPassword())
                .role(request.getRole())
                .primaryAddress(request.getPrimaryAddress())
                .taxId(request.getTaxId())
                .companyName(request.getCompanyName())
                .build();

        AuthenticationResult result = authenticationUseCasePort.register(command);
        return new ResponseEntity<>(AuthResponseDTO.fromResult(result), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        LoginCommand command = LoginCommand.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .build();

        AuthenticationResult result = authenticationUseCasePort.login(command);
        return ResponseEntity.ok(AuthResponseDTO.fromResult(result));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal UserDetailsAdapter principal) {
        if (principal == null || principal.getUser() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        authenticationUseCasePort.logout(principal.getUser().getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal UserDetailsAdapter principal,
            @Valid @RequestBody ChangePasswordRequestDTO request) {
        if (principal == null || principal.getUser() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        authenticationUseCasePort.changePassword(
                principal.getUser().getId(),
                request.getCurrentPassword(),
                request.getNewPassword());
        return ResponseEntity.noContent().build();
    }
}
