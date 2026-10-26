package com.nexusmarket.domain.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Contrato arquitectónico: todas las excepciones de dominio deben heredar de
 * {@link DomainException} y ninguna debe acoplarse a Spring. La traducción a
 * HTTP vive únicamente en el adaptador REST.
 */
class DomainExceptionContractTest {

    private static final List<Class<? extends DomainException>> EXCEPTIONS = List.of(
            BusinessRuleException.class,
            CategoryHasProductsException.class,
            CategoryNameAlreadyExistsException.class,
            DamagedInventoryException.class,
            DuplicateResourceException.class,
            InsufficientStockException.class,
            InvalidQuantityException.class,
            InvalidRefundAmountException.class,
            InvalidStatusTransitionException.class,
            InventoryNotInitializedException.class,
            InvoiceAlreadyExistsException.class,
            InvoiceNotPayableException.class,
            InvoiceNotRefundableException.class,
            PaymentGatewayException.class,
            ProductNotActiveException.class,
            ProductNotAvailableException.class,
            ProductSkuAlreadyExistsException.class,
            RefundAmountExceededException.class,
            RefundNotAllowedException.class,
            ResourceNotFoundException.class,
            ReturnAlreadyProcessedException.class,
            ReturnNotAllowedException.class,
            ReturnWindowExpiredException.class,
            SellerTaxIdAlreadyExistsException.class,
            ShipmentAlreadyExistsException.class,
            TrackingNotFoundException.class,
            UserAlreadyExistsException.class,
            UserEmailAlreadyExistsException.class,
            WarehouseCapacityExceededException.class,
            WarehouseNameAlreadyExistsException.class);

    @Test
    @DisplayName("Todas las excepciones heredan de DomainException y no de ResponseStatusException")
    void allDomainExceptionsExtendBase() {
        assertThat(EXCEPTIONS).allSatisfy(type -> {
            assertThat(DomainException.class).isAssignableFrom(type);
            assertThat(ResponseStatusException.class.isAssignableFrom(type)).isFalse();
        });
    }

    @Test
    @DisplayName("Ninguna excepción de dominio usa anotaciones de Spring")
    void domainExceptionsHaveNoSpringAnnotations() {
        for (Class<?> type : EXCEPTIONS) {
            assertNoSpringAnnotation(type.getAnnotations(), type.getSimpleName());
            for (Field field : type.getDeclaredFields()) {
                assertNoSpringAnnotation(field.getAnnotations(), type.getSimpleName() + "." + field.getName());
            }
            for (Method method : type.getDeclaredMethods()) {
                assertNoSpringAnnotation(method.getAnnotations(), type.getSimpleName() + "." + method.getName());
            }
            for (Constructor<?> constructor : type.getDeclaredConstructors()) {
                assertNoSpringAnnotation(constructor.getAnnotations(), type.getSimpleName() + " constructor");
            }
        }
    }

    @Test
    @DisplayName("DomainException es abstracta y no expone códigos HTTP")
    void baseExceptionIsFrameworkAgnostic() {
        assertThat(java.lang.reflect.Modifier.isAbstract(DomainException.class.getModifiers())).isTrue();
        assertThat(DomainException.class.getPackageName()).isEqualTo("com.nexusmarket.domain.exceptions");
        assertThat(DomainException.class.getDeclaredFields()).isEmpty();
    }

    private static void assertNoSpringAnnotation(Annotation[] annotations, String location) {
        for (Annotation annotation : annotations) {
            assertThat(annotation.annotationType().getName())
                    .as("Spring annotation on " + location)
                    .doesNotStartWith("org.springframework");
        }
    }
}
