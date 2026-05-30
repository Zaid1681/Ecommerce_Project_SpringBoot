//package com.platform.ecommerce.config;
//
//import com.platform.ecommerce.user.service.JwtService;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.ApplicationContext;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//
//@Component
//public class JwtFilter extends OncePerRequestFilter {
//
//    @Autowired
//    private JwtService jwtService;
//
//    @Autowired
//    ApplicationContext context;
//
//    // servlet has req and res object
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
//
//        String authHeader = request.getHeader("Authorization");
//        String token = null;
//        String userName = null;
//
//        if(authHeader !=null && authHeader.startsWith("Bearer ")){
//            token =authHeader.substring(7);
//            userName = jwtService.extractUserName(token);
//        }
//
//        if(userName !=null && SecurityContextHolder.getContext().getAuthentication() ==null){
//
//            UserDetails userDetails = context.getBean(UserDetailsService.class).loadUserByUsername(userName);
//
//            if(jwtService.validateToken(token , userDetails)){
//                UsernamePasswordAuthenticationToken authToken =
//                        new UsernamePasswordAuthenticationToken(userDetails , null , userDetails.getAuthorities());
//                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//                SecurityContextHolder.getContext().setAuthentication(authToken);
//
//            }
//        }
//        filterChain.doFilter(request, response);
//    }
//}


package com.platform.ecommerce.config;

import com.platform.ecommerce.user.service.JwtService;
import com.platform.ecommerce.user.service.SessionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private SessionService sessionService; // 🔥 NEW

    @Autowired
    ApplicationContext context;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String token = null;
        String userName = null;

        // 🔹 Extract JWT
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            userName = jwtService.extractUserName(token);
        }

        // 🔹 Proceed only if user not already authenticated
        if (userName != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = context
                    .getBean(UserDetailsService.class)
                    .loadUserByUsername(userName);

            // 🔴 Step 1: Validate JWT
            if (jwtService.validateToken(token, userDetails)) {

                // 🔴 Step 2: Validate Redis Session
                boolean isValidSession = sessionService.isSessionValid(userName, token);

                if (!isValidSession) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("Session expired or logged out");
                    return;
                }

                // 🔴 Step 3: Set Authentication
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authToken);
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid JWT");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}