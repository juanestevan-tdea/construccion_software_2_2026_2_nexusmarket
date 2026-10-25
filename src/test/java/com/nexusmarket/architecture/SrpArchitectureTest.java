package com.nexusmarket.architecture;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
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
    @DisplayName("los 28 servicios de dominio son POJO puros sin dependencias de Spring")
    void migratedDomainServicesDoNotDependOnSpring() throws Exception {
        // Se escanea el paquete completo en lugar de mantener una lista manual:
        // cualquier servicio nuevo queda cubierto automáticamente por el candado.
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter((metadataReader, metadataReaderFactory) -> true);

        Set<BeanDefinition> candidates =
                scanner.findCandidateComponents("com.nexusmarket.domain.services");

        List<Class<?>> services = new ArrayList<>();
        for (BeanDefinition definition : candidates) {
            Class<?> service = Class.forName(definition.getBeanClassName());
            if (service.getName().startsWith("com.nexusmarket.domain.services")) {
                services.add(service);
            }
        }

        // Guarda contra un escaneo vacío o incompleto: deben estar los 28 servicios.
        assertThat(services)
                .as("servicios de dominio escaneados en domain/services")
                .hasSize(28);

        List<String> violations = new ArrayList<>();
        for (Class<?> service : services) {
            collectSpringAnnotations(service.getAnnotations(), service.getSimpleName(), violations);

            for (Field field : service.getDeclaredFields()) {
                collectSpringType(field.getType(), service.getSimpleName() + "." + field.getName(), violations);
                collectSpringAnnotations(field.getAnnotations(),
                        service.getSimpleName() + "." + field.getName(), violations);
            }

            for (Method method : service.getDeclaredMethods()) {
                String location = service.getSimpleName() + "." + method.getName();
                collectSpringType(method.getReturnType(), location + " (retorno)", violations);
                for (Class<?> parameterType : method.getParameterTypes()) {
                    collectSpringType(parameterType, location + " (parámetro)", violations);
                }
                collectSpringAnnotations(method.getAnnotations(), location, violations);
            }

            for (Constructor<?> constructor : service.getDeclaredConstructors()) {
                String location = service.getSimpleName() + " (constructor)";
                for (Class<?> parameterType : constructor.getParameterTypes()) {
                    collectSpringType(parameterType, location + " (parámetro)", violations);
                }
                collectSpringAnnotations(constructor.getAnnotations(), location, violations);
            }
        }

        assertThat(violations)
                .as("el dominio no debe depender de Spring ni de jakarta.*")
                .isEmpty();
    }

    private static void collectSpringType(Class<?> type, String location, List<String> violations) {
        String typeName = type.getName();
        if (typeName.startsWith("org.springframework") || typeName.startsWith("jakarta.")) {
            violations.add(location + " : " + typeName);
        }
    }

    private static void collectSpringAnnotations(Annotation[] annotations, String location,
            List<String> violations) {
        for (Annotation annotation : annotations) {
            String annotationName = annotation.annotationType().getName();
            if (annotationName.startsWith("org.springframework")) {
                violations.add(location + " : @" + annotation.annotationType().getSimpleName());
            }
        }
    }
}
