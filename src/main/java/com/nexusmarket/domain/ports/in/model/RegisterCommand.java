package com.nexusmarket.domain.ports.in.model;

import com.nexusmarket.domain.valueobjects.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Comando de entrada para el registro de usuarios.
 *
 * <p>Pertenece a la capa de aplicación/dominio: no contiene anotaciones de
 * validación HTTP ni depende de los DTO del adaptador REST.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterCommand {

    private String email;
    private String fullName;
    private String password;
    private UserRole role;
    private String primaryAddress;
    private String taxId;
    private String companyName;
}
