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
import com.nexusmarket.common.exception.BusinessRuleException;
import com.nexusmarket.domain.models.User;
import com.nexusmarket.domain.ports.in.AuthenticationUseCasePort;
import com.nexusmarket.domain.ports.in.BuyerUseCasePort;
import com.nexusmarket.domain.ports.in.SellerUseCasePort;
import com.nexusmarket.domain.ports.in.UserUseCasePort;
import com.nexusmarket.domain.valueobjects.UserRole;
import com.nexusmarket.infrastructure.security.JwtService;
import com.nexusmarket.infrastructure.security.UserDetailsAdapter;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationUseCaseImpl implements AuthenticationUseCasePort {

    private final UserUseCasePort userUseCasePort;
    private final BuyerUseCasePort buyerUseCasePort;
    private final SellerUseCasePort sellerUseCasePort;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

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
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        UserDetailsAdapter principal = (UserDetailsAdapter) authentication.getPrincipal();
        User user = principal.getUser();

        String token = jwtService.generateToken(principal);
        return AuthResponseDTO.fromEntity(user, token, jwtExpiration);
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
