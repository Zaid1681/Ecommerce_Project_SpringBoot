package com.platform.ecommerce.user.controller;

import com.platform.ecommerce.user.dto.UserRequestDto;
import com.platform.ecommerce.user.dto.UserResponseDto;
import com.platform.ecommerce.user.entity.Users;
import com.platform.ecommerce.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping
    public UserResponseDto createUser(@Valid @RequestBody UserRequestDto user){
        return userService.createUser(user);
    }

    @GetMapping("/{id}")
    public UserResponseDto getUser(@PathVariable Long id){
        return userService.getUserById(id);
    }

    @GetMapping("/all")
    public List<UserResponseDto> getAllUser(){
        return userService.getAllUsers();
    }

    @PutMapping("/{id}")
    public UserResponseDto updateUser(@PathVariable Long id,
                                      @RequestBody UserRequestDto req){
        return userService.updateUser(id , req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id){
         String res = userService.deleteUser(id);
         return ResponseEntity.ok(res);
    }


}
