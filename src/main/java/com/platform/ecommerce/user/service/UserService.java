package com.platform.ecommerce.user.service;

import com.platform.ecommerce.user.dto.UserRequestDto;
import com.platform.ecommerce.user.dto.UserResponseDto;
import com.platform.ecommerce.user.entity.Users;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

public interface UserService {
    UserResponseDto createUser(UserRequestDto user);

    UserResponseDto getUserById(Long id);

    List<UserResponseDto> getAllUsers();

    UserResponseDto updateUser(Long id, UserRequestDto req);

    String deleteUser(Long id);
}
