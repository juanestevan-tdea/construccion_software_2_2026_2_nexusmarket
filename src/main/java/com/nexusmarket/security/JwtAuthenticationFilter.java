package com.nexusmarket.security;

import java.io.IOException;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Reads the {@code Authorization: Bearer <token>} header on every request and,
 * when the token is valid, populates the {@link SecurityContextHolder} with an
 * authenticated principal.
 *
 * <p>
 * The filter never rejects a request on its own. If the header is missing, the
 * token is malformed, expired or belongs to another user, it simply leaves the
 * security context empty and lets the authorization rules in
 * {@code SecurityConfig} answer with the appropriate 401/403.</p>
 *
 * <p>This class is deliberately <b>not</b> annotated with {@code @Component}: it is
 * instantiated as an explicit bean inside {@code SecurityConfig}. If Spring Boot also
 * auto-registered it as a servlet filter, the same logic would run twice on every
 * request.</p>
 */
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        // No bearer token: nothing to authenticate, continue down the chain.
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(BEARER_PREFIX.length());

        try {
            final String userEmail = jwtService.extractUsername(jwt);

            // Only authenticate when the context is still empty, so an already
            // authenticated request is never overwritten.
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(userEmail);

                if (jwtService.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception ex) {
            // Invalid token: clear any partial state and keep the request anonymous.
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
