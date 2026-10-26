package com.nexusmarket.infrastructure.security;

import com.nexusmarket.domain.services.SessionAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtService jwtService;
    private final SessionAuditService sessionAuditService;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final RestAccessDeniedHandler restAccessDeniedHandler;
    private final Environment environment;

    // Endpoints públicos (sin autenticación)
    private static final String[] PUBLIC_ENDPOINTS = {
        "/api/v1/auth/register",
        "/api/v1/auth/login",
        "/actuator/health",
        "/actuator/health/**"
    };

    // Documentación OpenAPI / Swagger UI (pública solo fuera de producción)
    private static final String[] SWAGGER_ENDPOINTS = {
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/swagger-ui.html"
    };

    // Endpoints de auth que requieren autenticación (logout, change-password)
    private static final String[] AUTHENTICATED_AUTH_ENDPOINTS = {
        "/api/v1/auth/logout",
        "/api/v1/auth/change-password"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                .exceptionHandling(ex -> ex
                .authenticationEntryPoint(restAuthenticationEntryPoint)
                .accessDeniedHandler(restAccessDeniedHandler))
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session
                        -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    if (isProdProfile()) {
                        // Swagger queda denegado en producción aunque springdoc se reactive por error.
                        auth.requestMatchers(SWAGGER_ENDPOINTS).denyAll();
                    } else {
                        // Documentación OpenAPI pública solo fuera de producción.
                        auth.requestMatchers(SWAGGER_ENDPOINTS).permitAll();
                    }

                    auth.requestMatchers(AUTHENTICATED_AUTH_ENDPOINTS).authenticated()
                            .requestMatchers(PUBLIC_ENDPOINTS).permitAll()

                            // Perfil propio accesible por cualquier autenticado
                            .requestMatchers(HttpMethod.GET, "/api/v1/admin/users/me").authenticated()

                            // Admin routes
                            .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                            // Supervisor routes
                            .requestMatchers("/api/v1/supervisor/**").hasRole("SUPERVISOR")

                            // Seller routes
                            .requestMatchers("/api/v1/seller/**").hasRole("SELLER")

                            // Buyer routes
                            .requestMatchers("/api/v1/buyer/**").hasRole("BUYER")

                            // Warehouse operator routes
                            .requestMatchers("/api/v1/warehouse/**").hasRole("WAREHOUSE_OPERATOR")

                            // Catalog (autenticado, cualquier rol)
                            .requestMatchers(HttpMethod.GET, "/api/v1/catalog/**").authenticated()

                            // Cualquier otra ruta requiere autenticación
                            .anyRequest().authenticated();
                })
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private boolean isProdProfile() {
        return Arrays.stream(environment.getActiveProfiles()).anyMatch("prod"::equalsIgnoreCase);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtService, customUserDetailsService, sessionAuditService);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
