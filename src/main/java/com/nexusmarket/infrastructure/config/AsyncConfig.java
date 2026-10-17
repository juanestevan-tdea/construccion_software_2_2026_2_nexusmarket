package com.nexusmarket.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuración de procesamiento asíncrono para la auditoría de sesiones.
 * Permite registrar eventos en MongoDB sin bloquear el flujo de autenticación.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Executor dedicado para tareas de auditoría de sesión.
     * Pequeño pool dado que la auditoría es escritura rápida y no crítica en la ruta crítica.
     */
    @Bean(name = "sessionAuditExecutor")
    public Executor sessionAuditExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("session-audit-");
        executor.initialize();
        return executor;
    }
}
