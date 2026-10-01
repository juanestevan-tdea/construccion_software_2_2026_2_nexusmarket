package com.nexusmarket.auth.service;

import com.nexusmarket.auth.dto.AuthResponse;
import com.nexusmarket.auth.dto.LoginRequest;
import com.nexusmarket.auth.dto.RegisterRequest;
import com.nexusmarket.common.exception.BusinessRuleException;
import com.nexusmarket.domain.models.Buyer;
import com.nexusmarket.domain.models.Seller;
import com.nexusmarket.domain.models.User;
import com.nexusmarket.domain.ports.in.BuyerUseCasePort;
import com.nexusmarket.domain.ports.in.SellerUseCasePort;
import com.nexusmarket.domain.ports.in.UserUseCasePort;
import com.nexusmarket.domain.valueobjects.UserRole;
import com.nexusmarket.security.JwtService;
import com.nexusmarket.security.UserDetailsAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestrates authentication: self-registration, credential validation and JWT
 * issuance.
 *
 * <p>
 * Password hashing itself lives in {@link UserUseCasePort}, which is the single
 * write path for the {@code usuarios} table. This service only decides
 * <em>what</em> to create (user plus its role-specific profile) and
 * <em>who</em> is allowed in.</p>
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserUseCasePort userUseCasePort;
    private final BuyerUseCasePort buyerUseCasePort;
    private final SellerUseCasePort sellerUseCasePort;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     * Registers a new BUYER or SELLER together with its role-specific profile.
     *
     * <p>
     * The whole operation runs in a single transaction: if the profile cannot
     * be created, the user row is rolled back. A {@code User} with role
     * {@code BUYER} and no {@code Buyer} row would be an inconsistent state
     * that breaks the ordering flow.</p>
     *
     * @param request the registration payload
     * @return the created user plus a freshly issued token
     * @throws BusinessRuleException when the role cannot self-register or a
     * required profile field is missing
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        UserRole role = request.getRole();

        if (role != UserRole.BUYER && role != UserRole.SELLER) {
            throw new BusinessRuleException("Self-registration is only allowed for BUYER and SELLER roles");
        }

        // userUseCasePort hashes the password before persisting.
        User user = userUseCasePort.createUser(
                request.getEmail(),
                request.getFullName(),
                request.getPassword(),
                role);

        if (role == UserRole.BUYER) {
            createBuyerProfile(user, request.getPrimaryAddress());
        } else {
            createSellerProfile(user, request.getTaxId(), request.getCompanyName());
        }

        String token = jwtService.generateToken(new UserDetailsAdapter(user));
        return AuthResponse.fromEntity(user, token, jwtExpiration);
    }

    /**
     * Validates credentials through the {@link AuthenticationManager} and
     * issues a JWT.
     *
     * @param request email and plain-text password
     * @return the authenticated user plus a freshly issued token
     */
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        UserDetailsAdapter principal = (UserDetailsAdapter) authentication.getPrincipal();
        User user = principal.getUser();

        String token = jwtService.generateToken(principal);
        return AuthResponse.fromEntity(user, token, jwtExpiration);
    }

    private void createBuyerProfile(User user, String primaryAddress) {
        if (primaryAddress == null || primaryAddress.isBlank()) {
            throw new BusinessRuleException("primaryAddress is required for BUYER registration");
        }

        buyerUseCasePort.createBuyer(user.getId(), primaryAddress);
    }

    private void createSellerProfile(User user, String taxId, String companyName) {
        if (taxId == null || taxId.isBlank()) {
            throw new BusinessRuleException("taxId is required for SELLER registration");
        }
        if (companyName == null || companyName.isBlank()) {
            throw new BusinessRuleException("companyName is required for SELLER registration");
        }

        sellerUseCasePort.createSeller(user.getId(), taxId, companyName);
    }
}

