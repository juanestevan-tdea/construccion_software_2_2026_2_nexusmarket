package com.nexusmarket.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.nexusmarket.domain.services.SessionAuditService;
import com.nexusmarket.infrastructure.security.UserDetailsAdapter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.Nonnull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;
    private final SessionAuditService sessionAuditService;

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request,
            @Nonnull HttpServletResponse response,
            @Nonnull FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(BEARER_PREFIX.length());

        try {
            final String userEmail = jwtService.extractUsername(jwt);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(userEmail);

                if (!userDetails.isEnabled()) {
                    String jti = jwtService.extractJti(jwt);
                    sessionAuditService.recordTokenRejected(userEmail, jti,
                            "user disabled",
                            HttpRequestMetadataExtractor.extractIp(request),
                            HttpRequestMetadataExtractor.extractUserAgent(request),
                            jwtService.extractVersion(jwt));
                    log.warn("JWT rechazado: usuario deshabilitado, email={}", userEmail);
                    SecurityContextHolder.clearContext();
                } else if (jwtService.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    // Token rechazado: distinguir entre versión obsoleta y token inválido/expirado
                    String jti = jwtService.extractJti(jwt);
                    try {
                        Integer tokenVersion = jwtService.extractVersion(jwt);
                        Integer userVersion = null;
                        if (userDetails instanceof UserDetailsAdapter adapter) {
                            userVersion = adapter.getUser().getTokenVersion();
                        }
                        if (tokenVersion != null && userVersion != null
                                && !tokenVersion.equals(userVersion)) {
                            sessionAuditService.recordTokenRejected(userEmail, jti,
                                    "token version obsolete",
                                    HttpRequestMetadataExtractor.extractIp(request),
                                    HttpRequestMetadataExtractor.extractUserAgent(request),
                                    tokenVersion);
                        } else {
                            sessionAuditService.recordTokenInvalid(jti,
                                    "invalid signature or expired",
                                    HttpRequestMetadataExtractor.extractIp(request),
                                    HttpRequestMetadataExtractor.extractUserAgent(request),
                                    tokenVersion);
                        }
                    } catch (Exception ex) {
                        sessionAuditService.recordTokenInvalid(jti, "token parse error",
                                HttpRequestMetadataExtractor.extractIp(request),
                                HttpRequestMetadataExtractor.extractUserAgent(request), null);
                    }
                    log.warn("JWT rechazado: jti={}, email={}", jti, userEmail);
                    SecurityContextHolder.clearContext();
                }
            }
        } catch (Exception ex) {
            // Cualquier fallo de parseo/validación → no autenticar.
            // Spring Security devolverá 401 a través del authenticationEntryPoint.
            try {
                String jti = jwtService.extractJti(jwt);
                sessionAuditService.recordTokenInvalid(jti, "token parse error",
                        HttpRequestMetadataExtractor.extractIp(request),
                        HttpRequestMetadataExtractor.extractUserAgent(request), null);
            } catch (Exception parseEx) {
                // No se pudo extraer jti; ignorar y continuar
            }
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
