package com.nexusmarket.users.controller;

import com.nexusmarket.users.domain.model.UserRole;
import com.nexusmarket.users.dto.request.UserCreateRequest;
import com.nexusmarket.users.dto.response.UserResponse;
import com.nexusmarket.users.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Crear un nuevo usuario usando DTO y Bean Validation
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
        UserResponse newUser = userService.createUser(request);
        return new ResponseEntity<>(newUser, HttpStatus.CREATED);
    }

    // Obtener usuario por ID
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse user = UserResponse.fromEntity(userService.getUserByIdOrThrow(id));
        return ResponseEntity.ok(user);
    }

    // Obtener usuario por email
    @GetMapping("/email")
    public ResponseEntity<UserResponse> getUserByEmail(@RequestParam String email) {
        UserResponse user = UserResponse.fromEntity(userService.getUserByEmailOrThrow(email));
        return ResponseEntity.ok(user);
    }

    // Listar todos los usuarios
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.findAll().stream()
                .map(UserResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(users);
    }

    // Listar usuarios por rol
    @GetMapping("/role/{role}")
    public ResponseEntity<List<UserResponse>> getUsersByRole(@PathVariable UserRole role) {
        List<UserResponse> users = userService.findByRole(role).stream()
                .map(UserResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(users);
    }

    // Bloquear un usuario
    @PatchMapping("/{id}/block")
    public ResponseEntity<UserResponse> blockUser(@PathVariable Long id) {
        UserResponse blockedUser = UserResponse.fromEntity(userService.blockUser(id));
        return ResponseEntity.ok(blockedUser);
    }

    // Activar un usuario
    @PatchMapping("/{id}/activate")
    public ResponseEntity<UserResponse> activateUser(@PathVariable Long id) {
        UserResponse activatedUser = UserResponse.fromEntity(userService.activateUser(id));
        return ResponseEntity.ok(activatedUser);
    }

    // Cambiar rol de un usuario
    @PatchMapping("/{id}/role")
    public ResponseEntity<UserResponse> changeUserRole(@PathVariable Long id, @RequestParam UserRole newRole) {
        UserResponse updatedUser = UserResponse.fromEntity(userService.changeRole(id, newRole));
        return ResponseEntity.ok(updatedUser);
    }
}