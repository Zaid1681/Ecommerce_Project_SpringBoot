package com.platform.ecommerce.config;

import com.platform.ecommerce.user.service.JwtService;
import com.platform.ecommerce.user.service.RateLimiterService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    // ============================
    // Dependencies
    // ============================

    @Autowired
    private RateLimiterService rateLimiterService;

    @Autowired
    private JwtService jwtService;

    // ============================
    // Public APIs (No Rate Limiting)
    // ============================

    private static final String[] PUBLIC_ENDPOINTS = {
            "/api/users/register",
            "/api/users/login",
            "/api/users/all",
            "/api/order",
            "/api/order/*",
            "/api/payment/webhook",
            "/api/order"
    };

    // ============================
    // Filter Execution Flow
    // ============================

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String requestPath = request.getRequestURI();

        // Step 1: Skip public endpoints
        if (isPublicEndpoint(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Step 2: Extract username from JWT
        String username = extractUsernameFromRequest(request);

        if (username == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // Step 3: Check rate limit for user
        if (!rateLimiterService.isAllowed(username)) {
            response.setStatus(429);
            response.getWriter().write("Too Many Requests");
            return;
        }

        // Step 4: Continue filter chain
        filterChain.doFilter(request, response);
    }

    // ============================
    // Helper Methods
    // ============================

    /**
     * Checks whether request belongs to public APIs
     */
    private boolean isPublicEndpoint(String path) {
        for (String endpoint : PUBLIC_ENDPOINTS) {
            if (path.startsWith(endpoint)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Extracts username from JWT token in Authorization header
     */
    private String extractUsernameFromRequest(HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtService.extractUserName(token);
        }

        return null;
    }
}