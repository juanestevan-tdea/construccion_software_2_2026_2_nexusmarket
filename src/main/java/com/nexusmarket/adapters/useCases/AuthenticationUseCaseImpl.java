package com.nexusmarket.adapters.useCases;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexusmarket.domain.exceptions.BusinessRuleException;
import com.nexusmarket.domain.models.User;
import com.nexusmarket.domain.ports.in.AuthenticationUseCasePort;
import com.nexusmarket.domain.ports.in.admin.UserUseCasePort;
import com.nexusmarket.domain.ports.in.buyer.BuyerUseCasePort;
import com.nexusmarket.domain.ports.in.model.AuthenticationResult;
import com.nexusmarket.domain.ports.in.model.LoginCommand;
import com.nexusmarket.domain.ports.in.model.RegisterCommand;
import com.nexusmarket.domain.ports.in.seller.SellerUseCasePort;
import com.nexusmarket.domain.services.PasswordChangeService;
import com.nexusmarket.domain.services.SessionAuditService;
import com.nexusmarket.domain.valueobjects.UserRole;
import com.nexusmarket.infrastructure.security.JwtService;
import com.nexusmarket.infrastructure.security.UserDetailsAdapter;

@Service
public class AuthenticationUseCaseImpl implements AuthenticationUseCasePort {

    private final UserUseCasePort userUseCasePort;
    private final BuyerUseCasePort buyerUseCasePort;
    private final SellerUseCasePort sellerUseCasePort;
    private final PasswordChangeService passwordChangeService;
    private final SessionAuditService sessionAuditService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final long jwtExpiration;

    public AuthenticationUseCaseImpl(
            UserUseCasePort userUseCasePort,
            BuyerUseCasePort buyerUseCasePort,
            SellerUseCasePort sellerUseCasePort,
            PasswordChangeService passwordChangeService,
            SessionAuditService sessionAuditService,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            @Value("${jwt.expiration}") long jwtExpiration) {
        this.userUseCasePort = userUseCasePort;
        this.buyerUseCasePort = buyerUseCasePort;
        this.sellerUseCasePort = sellerUseCasePort;
        this.passwordChangeService = passwordChangeService;
        this.sessionAuditService = sessionAuditService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.jwtExpiration = jwtExpiration;
    }

    @Override
    @Transactional
    public AuthenticationResult register(RegisterCommand command) {
        UserRole role = command.getRole();

        if (role != UserRole.BUYER && role != UserRole.SELLER) {
            throw new BusinessRuleException("Self-registration is only allowed for BUYER and SELLER roles");
        }

        User user = userUseCasePort.createUser(
                command.getEmail(),
                command.getFullName(),
                command.getPassword(),
                role);

        if (role == UserRole.BUYER) {
            createBuyerProfile(user, command.getPrimaryAddress());
        } else {
            createSellerProfile(user, command.getTaxId(), command.getCompanyName());
        }

        String token = jwtService.generateToken(new UserDetailsAdapter(user));
        return AuthenticationResult.from(user, token, jwtExpiration);
    }

    @Override
    public AuthenticationResult login(LoginCommand command) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(command.getEmail(), command.getPassword()));

            UserDetailsAdapter principal = (UserDetailsAdapter) authentication.getPrincipal();
            User user = principal.getUser();

            String token = jwtService.generateToken(principal);
            String jti = jwtService.extractJti(token);
            sessionAuditService.recordLogin(user.getEmail(), jti, null, null);

            return AuthenticationResult.from(user, token, jwtExpiration);
        } catch (AuthenticationException ex) {
            String reason = ex instanceof BadCredentialsException
                    ? "bad credentials"
                    : "account unavailable";
            sessionAuditService.recordLoginFailed(command.getEmail(), reason, null, null);
            throw ex;
        }
    }

    @Override
    @Transactional
    public void logout(Long userId) {
        User user = userUseCasePort.invalidateSessions(userId);
        sessionAuditService.recordLogout(user.getEmail(), null, null, null);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = passwordChangeService.changePassword(userId, currentPassword, newPassword);
        sessionAuditService.recordPasswordChange(user.getEmail(), null, null, null);
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

