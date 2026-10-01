package com.nexusmarket.adapters.rest.controllers;

import com.nexusmarket.adapters.rest.dtos.requests.UserCreateRequestDTO;
import com.nexusmarket.adapters.rest.dtos.responses.UserResponseDTO;
import com.nexusmarket.adapters.rest.mappers.UserRestMapper;
import com.nexusmarket.domain.models.User;
import com.nexusmarket.domain.ports.in.UserUseCasePort;
import com.nexusmarket.domain.valueobjects.UserRole;
import com.nexusmarket.security.UserDetailsAdapter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserUseCasePort userUseCasePort;

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserCreateRequestDTO request) {
        User newUser = userUseCasePort.createUser(request.getEmail(), request.getFullName(), request.getPassword(), request.getRole());
        return new ResponseEntity<>(UserRestMapper.toResponseDTO(newUser), HttpStatus.CREATED);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser(@AuthenticationPrincipal UserDetailsAdapter principal) {
        UserResponseDTO user = UserRestMapper.toResponseDTO(principal.getUser());
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        UserResponseDTO user = UserRestMapper.toResponseDTO(userUseCasePort.getUserByIdOrThrow(id));
        return ResponseEntity.ok(user);
    }

    @GetMapping("/email")
    public ResponseEntity<UserResponseDTO> getUserByEmail(@RequestParam String email) {
        UserResponseDTO user = UserRestMapper.toResponseDTO(userUseCasePort.getUserByEmailOrThrow(email));
        return ResponseEntity.ok(user);
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        List<UserResponseDTO> users = userUseCasePort.findAll().stream()
                .map(UserRestMapper::toResponseDTO)
                .toList();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/role/{role}")
    public ResponseEntity<List<UserResponseDTO>> getUsersByRole(@PathVariable UserRole role) {
        List<UserResponseDTO> users = userUseCasePort.findByRole(role).stream()
                .map(UserRestMapper::toResponseDTO)
                .toList();
        return ResponseEntity.ok(users);
    }

    @PatchMapping("/{id}/block")
    public ResponseEntity<UserResponseDTO> blockUser(@PathVariable Long id) {
        UserResponseDTO blockedUser = UserRestMapper.toResponseDTO(userUseCasePort.blockUser(id));
        return ResponseEntity.ok(blockedUser);
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<UserResponseDTO> activateUser(@PathVariable Long id) {
        UserResponseDTO activatedUser = UserRestMapper.toResponseDTO(userUseCasePort.activateUser(id));
        return ResponseEntity.ok(activatedUser);
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<UserResponseDTO> changeUserRole(@PathVariable Long id, @RequestParam UserRole newRole) {
        UserResponseDTO updatedUser = UserRestMapper.toResponseDTO(userUseCasePort.changeRole(id, newRole));
        return ResponseEntity.ok(updatedUser);
    }
}
