package com.moviedb.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Intercepts every request exactly once, extracts the Bearer token from the
 * Authorization header, validates it, and populates the SecurityContext.
 *
 * WHY OncePerRequestFilter?
 *   In a servlet chain, filters can be invoked multiple times via forwards/
 *   includes. OncePerRequestFilter guarantees a single execution per request,
 *   preventing double-authentication and race conditions on SecurityContext.
 *
 * Flow:
 *   1. Read "Authorization: Bearer <token>" header.
 *   2. Validate JWT signature + expiry via JwtUtil.
 *   3. Load UserDetails from DB (needed to get GrantedAuthorities).
 *   4. Create UsernamePasswordAuthenticationToken and set into SecurityContext.
 *   5. Continue filter chain — downstream security filters see an authenticated
 *      principal and allow/deny based on the configured rules.
 *
 * WHY check SecurityContextHolder.getContext().getAuthentication() == null?
 *   Only set authentication if not already set. Prevents overwriting a valid
 *   session-based auth (though we're stateless, this is defensive coding).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTH_HEADER   = "Authorization";

    @Override
    protected void doFilterInternal(HttpServletRequest  request,
                                    HttpServletResponse response,
                                    FilterChain         chain)
            throws ServletException, IOException {

        String token = extractToken(request);

        if (token != null && jwtUtil.validateToken(token)) {
            String username = jwtUtil.extractUsername(token);

            if (username != null &&
                SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        chain.doFilter(request, response);
    }

    /** Extracts the raw JWT from "Authorization: Bearer <token>" header. */
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(AUTH_HEADER);
        if (StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
