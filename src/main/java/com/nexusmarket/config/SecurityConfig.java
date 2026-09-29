package com.nexusmarket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.nexusmarket.security.CustomUserDetailsService;
import com.nexusmarket.security.JwtAuthenticationFilter;
import com.nexusmarket.security.JwtService;
import com.nexusmarket.security.RestAccessDeniedHandler;
import com.nexusmarket.security.RestAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;

/**
 * Central security configuration for the NexusMarket REST API.
 *
 * <p>
 * The API is fully stateless: no HTTP session is created and no CSRF token is
 * required, because authentication travels in the
 * {@code Authorization: Bearer <jwt>} header instead of a cookie.</p>
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtService jwtService;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final RestAccessDeniedHandler restAccessDeniedHandler;

    /**
     * Endpoints that must stay reachable without any authentication: login,
     * registration, OpenAPI/Swagger UI and the health probe.
     */
    private static final String[] PUBLIC_ENDPOINTS = {
        "/api/auth/**",
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/v3/api-docs/**",
        "/actuator/health",
        "/actuator/health/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                // Standard JSON error bodies for 401 (missing/invalid token) and
                // 403 (authenticated but not allowed) instead of empty responses.
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                        .accessDeniedHandler(restAccessDeniedHandler))
                // Stateless REST API: CSRF tokens are meaningless without cookies.
                .csrf(AbstractHttpConfigurer::disable)
                // Disable the default form login and HTTP Basic entry points.
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                // Never create or use an HTTP session.
                .sessionManagement(session
                        -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                // 1. Public endpoints.
                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                // 2. Any authenticated user may read its own profile.
                .requestMatchers(HttpMethod.GET, "/api/users/me").authenticated()
                // 3. User administration is restricted to ADMIN.
                .requestMatchers("/api/users/**").hasRole("ADMIN")
                // 4. Audit trail: ADMIN or SUPERVISOR only.
                .requestMatchers("/api/audit/**").hasAnyRole("ADMIN", "SUPERVISOR")
                // 5. Catalog read operations are open to any authenticated user.
                .requestMatchers(HttpMethod.GET,
                        "/api/products/**",
                        "/api/categories/**",
                        "/api/catalog/**",
                        "/api/warehouses/**").authenticated()
                // 6. Everything else requires a valid JWT.
                .anyRequest().authenticated())
                // Register our JWT filter before the standard username/password filter.
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtService, customUserDetailsService);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
