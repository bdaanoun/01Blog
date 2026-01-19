package com.o1blog._blog.security;

import com.o1blog._blog.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
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
                logger.warn("Invalid JWT token");
                SecurityContextHolder.clearContext();
            }
        }

        // Validate token and set authentication
        // if (jwt != null && SecurityContextHolder.getContext().getAuthentication() ==
        // null) {
        // try {
        // Long userId = jwtUtil.extractUserId(jwt);
        // if (userId != null) {
        // CustomUserDetails userDetails = userDetailsService.loadUserById(userId);

        // if (jwtUtil.validateToken(jwt, userDetails)) {
        // UsernamePasswordAuthenticationToken authenticationToken = new
        // UsernamePasswordAuthenticationToken(
        // userDetails, null, userDetails.getAuthorities());

        // authenticationToken.setDetails(
        // new WebAuthenticationDetailsSource().buildDetails(request));

        // SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        // }
        // }
        // } catch (Exception e) {
        // // IMPORTANT: do NOT crash the request (prevents "fake CORS" errors)
        // SecurityContextHolder.clearContext();
        // logger.warn("JWT auth failed: {} " + e.getMessage());
        // }
        // }

        if (username != null &&
                SecurityContextHolder.getContext().getAuthentication() == null) {
            Long userId = jwtUtil.extractUserId(jwt);
            CustomUserDetails userDetails = userDetailsService.loadUserById(userId);

            boolean isValid = jwtUtil.validateToken(jwt, userDetails);
            // System.out.println("Token valid: " + isValid);

            if (isValid) {
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities());

                authenticationToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                // System.out.println("Authentication set in SecurityContext");
                System.out.println("Auth details: " + SecurityContextHolder.getContext().getAuthentication());
            } else {
                System.out.println("Token validation FAILED");
            }
        }

        filterChain.doFilter(request, response);
    }
}