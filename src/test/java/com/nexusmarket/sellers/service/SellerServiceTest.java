package com.nexusmarket.sellers.service;

import com.nexusmarket.adapters.useCases.SellerUseCaseImpl;
import com.nexusmarket.domain.exceptions.SellerTaxIdAlreadyExistsException;
import com.nexusmarket.domain.models.Seller;
import com.nexusmarket.domain.models.User;
import com.nexusmarket.domain.ports.in.seller.SellerUseCasePort;
import com.nexusmarket.domain.ports.out.SellerRepositoryPort;
import com.nexusmarket.domain.ports.out.UserRepositoryPort;
import com.nexusmarket.domain.services.SellerDomainService;
import com.nexusmarket.domain.valueobjects.UserRole;
import com.nexusmarket.domain.valueobjects.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SellerServiceTest {

    @Mock
    private SellerRepositoryPort sellerRepositoryPort;

    @Mock
    private UserRepositoryPort userRepositoryPort;

    private SellerUseCasePort sellerUseCase;

    private User sellerUser;

    @BeforeEach
    void setUp() {
        sellerUser = User.builder()
                .id(1L)
                .email("seller@test.com")
                .fullName("Seller")
                .role(UserRole.SELLER)
                .status(UserStatus.ACTIVE)
                .build();

        SellerDomainService domainService = new SellerDomainService(sellerRepositoryPort, userRepositoryPort);
        sellerUseCase = new SellerUseCaseImpl(domainService, sellerRepositoryPort);
    }

    @Test
    @DisplayName("createSeller crea el vendedor cuando el taxId no está duplicado")
    void createSeller_succeeds_whenTaxIdIsUnique() {
        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(sellerUser));
        when(sellerRepositoryPort.findByUserId(1L)).thenReturn(Optional.empty());
        when(sellerRepositoryPort.findByTaxId("TAX-1")).thenReturn(Optional.empty());
        when(sellerRepositoryPort.save(any(Seller.class))).thenAnswer(invocation -> {
            Seller seller = invocation.getArgument(0);
            seller.setId(10L);
            return seller;
        });

        Seller result = sellerUseCase.createSeller(1L, "TAX-1", "Tienda Nexus");

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("TAX-1", result.getTaxId());
    }

    @Test
    @DisplayName("createSeller lanza SellerTaxIdAlreadyExistsException cuando el taxId existe")
    void createSeller_throwsSellerTaxIdAlreadyExistsException_whenTaxIdExists() {
        Seller existing = Seller.builder()
                .id(10L)
                .userId(2L)
                .taxId("TAX-1")
                .companyName("Otra tienda")
                .active(true)
                .build();

        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(sellerUser));
        when(sellerRepositoryPort.findByUserId(1L)).thenReturn(Optional.empty());
        when(sellerRepositoryPort.findByTaxId("TAX-1")).thenReturn(Optional.of(existing));

        SellerTaxIdAlreadyExistsException exception = assertThrows(
                SellerTaxIdAlreadyExistsException.class,
                () -> sellerUseCase.createSeller(1L, "TAX-1", "Tienda Nexus"));

        assertEquals("Seller with taxId 'TAX-1' already exists", exception.getMessage());
        verify(sellerRepositoryPort, never()).save(any(Seller.class));
    }
}
