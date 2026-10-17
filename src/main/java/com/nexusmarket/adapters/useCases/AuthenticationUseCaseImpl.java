package com.nexusmarket.adapters.useCases;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexusmarket.adapters.rest.dtos.requests.LoginRequestDTO;
import com.nexusmarket.adapters.rest.dtos.requests.RegisterRequestDTO;
import com.nexusmarket.adapters.rest.dtos.responses.AuthResponseDTO;
import com.nexusmarket.domain.exceptions.BusinessRuleException;
import com.nexusmarket.domain.models.User;
import com.nexusmarket.domain.ports.in.AuthenticationUseCasePort;
import com.nexusmarket.domain.ports.in.admin.UserUseCasePort;
import com.nexusmarket.domain.ports.in.buyer.BuyerUseCasePort;
import com.nexusmarket.domain.ports.in.seller.SellerUseCasePort;
import com.nexusmarket.domain.services.SessionAuditService;
import com.nexusmarket.domain.services.UserManagementService;
import com.nexusmarket.domain.valueobjects.UserRole;
import com.nexusmarket.infrastructure.security.JwtService;
import com.nexusmarket.infrastructure.security.UserDetailsAdapter;
import org.springframework.security.authentication.BadCredentialsException;

@Service
public class AuthenticationUseCaseImpl implements AuthenticationUseCasePort {

    private final UserUseCasePort userUseCasePort;
    private final BuyerUseCasePort buyerUseCasePort;
    private final SellerUseCasePort sellerUseCasePort;    private final UserManagementService userManagementService;
    private final SessionAuditService sessionAuditService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final long jwtExpiration;

    public AuthenticationUseCaseImpl(
            UserUseCasePort userUseCasePort,
            BuyerUseCasePort buyerUseCasePort,
            SellerUseCasePort sellerUseCasePort,
            UserManagementService userManagementService,
            SessionAuditService sessionAuditService,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            @Value("${jwt.expiration}") long jwtExpiration) {
        this.userUseCasePort = userUseCasePort;
        this.buyerUseCasePort = buyerUseCasePort;
        this.sellerUseCasePort = sellerUseCasePort;
        this.userManagementService = userManagementService;
        this.sessionAuditService = sessionAuditService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.jwtExpiration = jwtExpiration;
    }

    @Override
    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request) {
        UserRole role = request.getRole();

        if (role != UserRole.BUYER && role != UserRole.SELLER) {
            throw new BusinessRuleException("Self-registration is only allowed for BUYER and SELLER roles");
        }

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
        return AuthResponseDTO.fromEntity(user, token, jwtExpiration);
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

            UserDetailsAdapter principal = (UserDetailsAdapter) authentication.getPrincipal();
            User user = principal.getUser();

            String token = jwtService.generateToken(principal);
            String jti = jwtService.extractJti(token);
            sessionAuditService.recordLogin(user.getEmail(), jti, null);

            return AuthResponseDTO.fromEntity(user, token, jwtExpiration);
        } catch (BadCredentialsException ex) {
            sessionAuditService.recordLoginFailed(request.getEmail(), "bad credentials", null);
            throw ex;
        }
    }

    @Override
    @Transactional
    public void logout(Long userId) {
        userUseCasePort.invalidateSessions(userId);
        User user = userUseCasePort.getUserByIdOrThrow(userId);
        sessionAuditService.recordLogout(user.getEmail(), null, null);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        userManagementService.changePassword(userId, currentPassword, newPassword);
        User user = userUseCasePort.getUserByIdOrThrow(userId);
        sessionAuditService.recordPasswordChange(user.getEmail(), null, null);
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

