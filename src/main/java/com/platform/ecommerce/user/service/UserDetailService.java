package com.platform.ecommerce.user.service;

import com.platform.ecommerce.user.dto.UserRequestDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class UserDetailService {
    @Autowired
    AuthenticationManager authenticationManager;

    public String getAuthenticate(UserRequestDto user) {

        // used after /login hit to verify user credentials and it calls internally  --> Authprovider by passing username and password

        Authentication authentication  =
                authenticationManager
                        .authenticate(new UsernamePasswordAuthenticationToken(user.getName(), user.getPassword()));

        if(authentication.isAuthenticated()){
            return "Success";
        }
        return "Failure";

    }
}
