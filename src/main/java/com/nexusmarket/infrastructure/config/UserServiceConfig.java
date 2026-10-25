package com.nexusmarket.infrastructure.config;

import com.nexusmarket.domain.ports.out.BuyerRepositoryPort;
import com.nexusmarket.domain.ports.out.PasswordHasherPort;
import com.nexusmarket.domain.ports.out.UserRepositoryPort;
import com.nexusmarket.domain.services.AddBuyerAddressService;
import com.nexusmarket.domain.services.ChangeBuyerCommercialStatusService;
import com.nexusmarket.domain.services.CreateBuyerService;
import com.nexusmarket.domain.services.PasswordChangeService;
import com.nexusmarket.domain.services.SessionInvalidationService;
import com.nexusmarket.domain.services.UserCreationService;
import com.nexusmarket.domain.services.UserQueryService;
import com.nexusmarket.domain.services.UserStatusService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registro de los servicios de dominio de usuarios, autenticación, compradores y
 * sesiones.
 *
 * <p>Los servicios son POJO puros: no llevan {@code @Service} ni
 * {@code @Transactional}. Las transacciones viven en las fachadas de
 * {@code adapters/useCases}.</p>
 */
@Configuration
public class UserServiceConfig {

    @Bean
    public UserCreationService userCreationService(UserRepositoryPort userRepositoryPort,
            PasswordHasherPort passwordHasherPort) {
        return new UserCreationService(userRepositoryPort, passwordHasherPort);
    }

    @Bean
    public PasswordChangeService passwordChangeService(UserRepositoryPort userRepositoryPort,
            PasswordHasherPort passwordHasherPort) {
        return new PasswordChangeService(userRepositoryPort, passwordHasherPort);
    }

    @Bean
    public CreateBuyerService createBuyerService(BuyerRepositoryPort buyerRepositoryPort,
            UserRepositoryPort userRepositoryPort) {
        return new CreateBuyerService(buyerRepositoryPort, userRepositoryPort);
    }

    @Bean
    public AddBuyerAddressService addBuyerAddressService(BuyerRepositoryPort buyerRepositoryPort) {
        return new AddBuyerAddressService(buyerRepositoryPort);
    }

    @Bean
    public ChangeBuyerCommercialStatusService changeBuyerCommercialStatusService(
            BuyerRepositoryPort buyerRepositoryPort) {
        return new ChangeBuyerCommercialStatusService(buyerRepositoryPort);
    }

    @Bean
    public UserQueryService userQueryService(UserRepositoryPort userRepositoryPort) {
        return new UserQueryService(userRepositoryPort);
    }

    @Bean
    public UserStatusService userStatusService(UserRepositoryPort userRepositoryPort) {
        return new UserStatusService(userRepositoryPort);
    }

    @Bean
    public SessionInvalidationService sessionInvalidationService(
            UserRepositoryPort userRepositoryPort) {
        return new SessionInvalidationService(userRepositoryPort);
    }
}
