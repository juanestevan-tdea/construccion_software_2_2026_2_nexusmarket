package com.nexusmarket.adapters.rest.dtos.requests;

import com.nexusmarket.domain.valueobjects.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDTO {

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

    @Size(max = 255, message = "Primary address cannot exceed 255 characters")
    private String primaryAddress;

    @Size(max = 50, message = "Tax ID cannot exceed 50 characters")
    private String taxId;

    @Size(max = 150, message = "Company name cannot exceed 150 characters")
    private String companyName;
}
