package com.nexusmarket.users.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexusmarket.users.domain.model.Buyer;
import com.nexusmarket.users.domain.model.BuyerCommercialStatus;
import com.nexusmarket.users.domain.model.User;
import com.nexusmarket.users.domain.model.UserRole;
import com.nexusmarket.users.domain.repository.BuyerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BuyerService {

    private final BuyerRepository buyerRepository;
    private final UserService userService;

    // Crear un comprador a partir de un usuario existente
    @Transactional
    public Buyer createBuyer(Long userId, String primaryAddress) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // Validar que el usuario tenga rol BUYER
        if (!user.getRole().equals(UserRole.BUYER)) {
            throw new IllegalArgumentException("User is not a BUYER. Current role: " + user.getRole());
        }

        Buyer buyer = Buyer.builder()
                .user(user)
                .primaryAddress(primaryAddress)
                .commercialStatus(BuyerCommercialStatus.ACTIVE)
                .build();

        return buyerRepository.save(buyer);
    }

    // Buscar comprador por ID
    public Optional<Buyer> findById(Long id) {
        return buyerRepository.findById(id);
    }

    // Buscar comprador por usuario
    public Optional<Buyer> findByUser(User user) {
        return buyerRepository.findByUser(user);
    }

    // Listar todos los compradores
    public List<Buyer> findAll() {
        return buyerRepository.findAll();
    }

    // Listar compradores por estado comercial
    public List<Buyer> findByCommercialStatus(BuyerCommercialStatus status) {
        return buyerRepository.findByCommercialStatus(status);
    }

    // Agregar dirección adicional
    @Transactional
    public Buyer addAdditionalAddress(Long id, String address) {
        Buyer buyer = buyerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Buyer not found: " + id));
        buyer.addAdditionalAddress(address);
        return buyerRepository.save(buyer);
    }

    // Cambiar estado comercial del comprador
    @Transactional
    public Buyer changeCommercialStatus(Long id, BuyerCommercialStatus newStatus) {
        Buyer buyer = buyerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Buyer not found: " + id));
        buyer.setCommercialStatus(newStatus);
        return buyerRepository.save(buyer);
    }
}