package com.o1blog._blog.security;

import com.o1blog._blog.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        System.out.println("METHOD: " + request.getMethod());
        System.out.println("URI: " + request.getRequestURI());
        System.out.println("HEADER: " + request.getHeader("Authorization"));

        // Skip JWT validation for public endpoints
        String path = request.getRequestURI();
        if (path.equals("/api/register") || path.equals("/api/login")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        // Extract JWT token from Authorization header
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                logger.error("JWT Token extraction failed", e);
            }
            System.out.println("jwt: " + jwt);
            System.out.println("username: " + username);

        }

        // Validate token and set authentication
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            CustomUserDetails userDetails = userDetailsService.loadUserByUsername(username);
            System.out.println("UserDetails loaded: " + userDetails);
            System.out.println("UserDetails username: " + userDetails.getUsername());
            System.out.println("UserDetails authorities: " + userDetails.getAuthorities());

            boolean isValid = jwtUtil.validateToken(jwt, userDetails);
            System.out.println("Token valid: " + isValid);

            if (isValid) {
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities());

                authenticationToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                System.out.println("Authentication set in SecurityContext");
                System.out.println("Auth details: " + SecurityContextHolder.getContext().getAuthentication());
            } else {
                System.out.println("Token validation FAILED");
            }
        }

        filterChain.doFilter(request, response);
    }
}