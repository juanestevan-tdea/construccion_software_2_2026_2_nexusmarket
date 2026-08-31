package com.nexusmarket.users.controller;

import com.nexusmarket.users.domain.model.User;
import com.nexusmarket.users.domain.model.UserRole;
import com.nexusmarket.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Crear un nuevo usuario
    @PostMapping
    public ResponseEntity<User> createUser(@RequestParam String email,
                                           @RequestParam String fullName,
                                           @RequestParam String password,
                                           @RequestParam UserRole role) {
        User newUser = userService.createUser(email, fullName, password, role);
        return new ResponseEntity<>(newUser, HttpStatus.CREATED);
    }

    // Obtener usuario por ID
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userService.findById(id);
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Obtener usuario por email
    @GetMapping("/email")
    public ResponseEntity<User> getUserByEmail(@RequestParam String email) {
        Optional<User> user = userService.findByEmail(email);
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Listar todos los usuarios
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.findAll());
    }

    // Listar usuarios por rol
    @GetMapping("/role/{role}")
    public ResponseEntity<List<User>> getUsersByRole(@PathVariable UserRole role) {
        return ResponseEntity.ok(userService.findByRole(role));
    }

    // Bloquear un usuario
    @PatchMapping("/{id}/block")
    public ResponseEntity<User> blockUser(@PathVariable Long id) {
        User blockedUser = userService.blockUser(id);
        return ResponseEntity.ok(blockedUser);
    }

    // Activar un usuario
    @PatchMapping("/{id}/activate")
    public ResponseEntity<User> activateUser(@PathVariable Long id) {
        User activatedUser = userService.activateUser(id);
        return ResponseEntity.ok(activatedUser);
    }

    // Cambiar rol de un usuario
    @PatchMapping("/{id}/role")
    public ResponseEntity<User> changeUserRole(@PathVariable Long id, @RequestParam UserRole newRole) {
        User updatedUser = userService.changeRole(id, newRole);
        return ResponseEntity.ok(updatedUser);
    }
}