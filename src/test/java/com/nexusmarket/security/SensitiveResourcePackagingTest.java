package com.nexusmarket.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Guard de regresión: los perfiles locales y archivos de entorno no deben
 * terminar en el classpath de la aplicación empaquetada.
 */
class SensitiveResourcePackagingTest {

    @Test
    @DisplayName("los archivos locales y .env no están en el classpath")
    void localSecretFilesAreNotPackaged() {
        ClassLoader classLoader = getClass().getClassLoader();

        assertThat(classLoader.getResource("application-local.properties")).isNull();
        assertThat(classLoader.getResource("application-local.yml")).isNull();
        assertThat(classLoader.getResource("application-dev.properties")).isNull();
        assertThat(classLoader.getResource("application-dev.yml")).isNull();
        assertThat(classLoader.getResource(".env")).isNull();
    }
}
