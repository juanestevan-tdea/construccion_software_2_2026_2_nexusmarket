package com.nexusmarket.architecture;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Contratos arquitectónicos de SRP y hexagonal.
 *
 * <p>Los controladores REST solo pueden depender de puertos de entrada; no deben
 * inyectar repositorios de salida ni repositorios Mongo. Los servicios de
 * dominio no deben depender de {@code jakarta.servlet}.</p>
 */
class SrpArchitectureTest {

    @Test
    @DisplayName("ningún controlador REST inyecta repositorios de salida o Mongo")
    void controllersDoNotDependOnRepositories() throws Exception {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter((metadataReader, metadataReaderFactory) ->
                metadataReader.getAnnotationMetadata().hasAnnotation(
                        "org.springframework.web.bind.annotation.RestController"));

        Set<BeanDefinition> controllers =
                scanner.findCandidateComponents("com.nexusmarket.adapters.rest.controllers");

        List<String> violations = new ArrayList<>();
        for (BeanDefinition definition : controllers) {
            Class<?> controller = Class.forName(definition.getBeanClassName());
            for (Field field : controller.getDeclaredFields()) {
                String type = field.getType().getName();
                if (type.startsWith("com.nexusmarket.domain.ports.out")
                        || type.endsWith("MongoRepository")) {
                    violations.add(controller.getSimpleName() + "." + field.getName()
                            + " : " + type);
                }
            }
        }

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("ningún servicio de dominio depende de la API de servlet")
    void domainServicesDoNotDependOnServletApi() throws Exception {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter((metadataReader, metadataReaderFactory) -> true);

        Set<BeanDefinition> candidates =
                scanner.findCandidateComponents("com.nexusmarket.domain.services");

        List<String> violations = new ArrayList<>();
        for (BeanDefinition definition : candidates) {
            Class<?> service = Class.forName(definition.getBeanClassName());
            if (!service.getName().startsWith("com.nexusmarket.domain.services")) {
                continue;
            }
            for (Field field : service.getDeclaredFields()) {
                if (field.getType().getName().startsWith("jakarta.servlet")) {
                    violations.add(service.getSimpleName() + "." + field.getName()
                            + " : " + field.getType().getName());
                }
            }
            for (Method method : service.getDeclaredMethods()) {
                for (Class<?> parameterType : method.getParameterTypes()) {
                    if (parameterType.getName().startsWith("jakarta.servlet")) {
                        violations.add(service.getSimpleName() + "." + method.getName()
                                + " : " + parameterType.getName());
                    }
                }
            }
        }

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("los servicios migrados en la fase P2-03 no dependen de Spring")
    void migratedDomainServicesDoNotDependOnSpring() throws Exception {
        List<Class<?>> migrated = List.of(
                Class.forName("com.nexusmarket.domain.services.UserCreationService"),
                Class.forName("com.nexusmarket.domain.services.PasswordChangeService"),
                Class.forName("com.nexusmarket.domain.services.InvoiceGenerateService"),
                Class.forName("com.nexusmarket.domain.services.InvoiceVoidService"),
                Class.forName("com.nexusmarket.domain.services.RefundProcessService"),
                Class.forName("com.nexusmarket.domain.services.RefundApproveService"),
                Class.forName("com.nexusmarket.domain.services.ProductDomainService"),
                Class.forName("com.nexusmarket.domain.services.CategoryDomainService"),
                Class.forName("com.nexusmarket.domain.services.InventoryManagementService"),
                Class.forName("com.nexusmarket.domain.services.InventoryConsultService"),
                Class.forName("com.nexusmarket.domain.services.WarehouseDomainService"));

        List<String> violations = new ArrayList<>();
        for (Class<?> service : migrated) {
            for (Annotation annotation : service.getAnnotations()) {
                if (annotation.annotationType().getName().startsWith("org.springframework")) {
                    violations.add(service.getSimpleName() + " : @" + annotation.annotationType().getSimpleName());
                }
            }
            for (Field field : service.getDeclaredFields()) {
                String type = field.getType().getName();
                if (type.startsWith("org.springframework") || type.startsWith("jakarta.")) {
                    violations.add(service.getSimpleName() + "." + field.getName() + " : " + type);
                }
            }
        }

        assertThat(violations).isEmpty();
    }
}
