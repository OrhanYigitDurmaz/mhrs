package com.mhrs.auth.infrastructure.security;

import com.mhrs.auth.domain.UserRole;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtDecoder jwtDecoder;

    public JwtAuthenticationFilter(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring("Bearer ".length()).trim();
        try {
            Jwt jwt = jwtDecoder.decode(token);
            Authentication authentication = buildAuthentication(jwt);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (JwtException ex) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
        }
    }

    private Authentication buildAuthentication(Jwt jwt) {
        String userId = jwt.getSubject();
        String email = jwt.getClaim("email");
        String roleClaim = jwt.getClaim("role");
        Boolean emailVerified = jwt.getClaim("email_verified");
        String sessionId = jwt.getClaim("sid");
        if (userId == null || roleClaim == null || sessionId == null) {
            throw new JwtException("Missing required claims.");
        }
        UserRole role = UserRole.valueOf(roleClaim);
        AuthPrincipal principal = new AuthPrincipal(
            userId,
            email,
            role,
            emailVerified != null && emailVerified,
            sessionId
        );
        List<SimpleGrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority("ROLE_" + role.name())
        );
        return new UsernamePasswordAuthenticationToken(
            principal,
            jwt,
            authorities
        );
    }
}
