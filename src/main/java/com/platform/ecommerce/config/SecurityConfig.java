package com.platform.ecommerce.config;

import com.platform.ecommerce.user.service.MyUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // ============================
    // Dependencies
    // ============================

    @Autowired
    private MyUserDetailsService userDetailsService;

    @Autowired
    private JwtFilter jwtFilter;

    @Autowired
    private RateLimitFilter rateLimitFilter;

    // ============================
    // Public (Unauthenticated) APIs
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
    // Security Filter Chain
    // ============================

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        return http
                // 1. Disable CSRF (Not needed for stateless APIs)
                .csrf(csrf -> csrf.disable())

                // 2. Define authorization rules
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .anyRequest().authenticated()
                )
//                // 🔴 Allow EVERYTHING
//                .authorizeHttpRequests(auth -> auth
//                        .anyRequest().permitAll()
//                )
                .cors(Customizer.withDefaults())

                // 3. Enable basic auth (optional, useful for testing)
//                .httpBasic(Customizer.withDefaults())

                // 4. Make session stateless (JWT-based auth)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 5. Add custom filters (Order matters!)
                // Rate limiter should execute BEFORE authentication
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)

                // JWT filter to validate token and set security context
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

                .build();
    }

    // ============================
    // Authentication Provider
    // ============================

    /**
     * DAO Authentication Provider:
     * - Uses UserDetailsService to fetch user
     * - Uses BCrypt to validate password
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(new BCryptPasswordEncoder(12));

        return provider;
    }

    // ============================
    // Authentication Manager
    // ============================

    /**
     * Used during login to authenticate user
     * and generate JWT token
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}