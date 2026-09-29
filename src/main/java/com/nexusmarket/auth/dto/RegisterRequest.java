package com.nexusmarket.auth.dto;

import com.nexusmarket.users.domain.model.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload accepted by {@code POST /api/auth/register}.
 *
 * <p>
 * Only {@code BUYER} and {@code SELLER} can self-register. Every other role
 * ({@code ADMIN}, {@code SUPERVISOR}, {@code WAREHOUSE_OPERATOR}) must be
 * created by an administrator through {@code POST /api/users}.</p>
 *
 * <h2>Conditional required fields</h2>
 * <p>
 * Jakarta Bean Validation cannot express "required only when another field has
 * a given value" without validation groups, so the cross-field rules are
 * enforced in {@code AuthService.register}:</p>
 * <ul>
 * <li>{@code role == BUYER} &rarr; {@link #primaryAddress} is mandatory (the
 * {@code compradores.primaryAddress} column is {@code NOT NULL}).</li>
 * <li>{@code role == SELLER} &rarr; both {@link #taxId} and
 * {@link #companyName} are mandatory (both columns are {@code NOT NULL} in
 * {@code vendedores}).</li>
 * </ul>
 * <p>
 * Violations surface as a {@code BusinessRuleException} (HTTP 422) with a
 * message naming the missing field.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name cannot exceed 100 characters")
    private String fullName;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;

    @NotNull(message = "Role is required")
    private UserRole role;

    /**
     * Mandatory when {@code role == BUYER}; ignored otherwise.
     */
    @Size(max = 255, message = "Primary address cannot exceed 255 characters")
    private String primaryAddress;

    /**
     * Mandatory when {@code role == SELLER}; ignored otherwise.
     */
    @Size(max = 50, message = "Tax ID cannot exceed 50 characters")
    private String taxId;

    /**
     * Mandatory when {@code role == SELLER}; ignored otherwise.
     */
    @Size(max = 150, message = "Company name cannot exceed 150 characters")
    private String companyName;
}
