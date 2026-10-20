package com.nexusmarket.domain.ports.in.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Comando de entrada para el inicio de sesión.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginCommand {

    private String email;
    private String password;
}
