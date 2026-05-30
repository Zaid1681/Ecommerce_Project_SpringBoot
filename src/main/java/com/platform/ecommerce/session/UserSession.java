package com.platform.ecommerce.session;

import com.platform.ecommerce.user.entity.Users;
import com.platform.ecommerce.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserSession {
    @Autowired
    private UserRepository userRepo;
    public Users getCurrentUser() {

        String userName = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return Optional.ofNullable(userRepo.findByUsername(userName))
                .orElseThrow(() ->
                        new RuntimeException("Session not valid"));
    }
}
