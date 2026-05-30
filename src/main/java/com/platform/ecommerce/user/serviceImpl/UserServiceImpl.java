package com.platform.ecommerce.user.serviceImpl;

import com.platform.ecommerce.exceptons.UserNotFoundException;
import com.platform.ecommerce.user.dto.UserRequestDto;
import com.platform.ecommerce.user.dto.UserResponseDto;
import com.platform.ecommerce.user.entity.Users;
import com.platform.ecommerce.user.repository.UserRepository;
import com.platform.ecommerce.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    UserRepository repo;

    @Override
    @CacheEvict(cacheNames = "users" , key="#result.id") // expression  language :- #id
    public UserResponseDto createUser(UserRequestDto req) {
        log.info("createUser called for username={}", req.getName());
        Optional<Users> usr = repo.findByEmail(req.getEmail());
        if(usr.isPresent()){
            throw new RuntimeException("Data Already Exist");
        }
        if(repo.existsByUsername(req.getName())){
            throw new RuntimeException("Data Already Exist");
        }

        Users users  = Users.builder()
                .username(req.getName())
                .email(req.getEmail())
                .password(req.getPassword())
                .isActive(true)
                .build();
        BCryptPasswordEncoder brcypt  = new BCryptPasswordEncoder(12);
        users.setPassword(brcypt.encode(req.getPassword()));
        repo.save(users);
        log.info("User created with id={}", users.getId());
        return mapToResponse(users);

    }

    @Override
    @Cacheable(cacheNames = "users" , key="#id") // expression  language :- #id
    public UserResponseDto getUserById(Long id) {
        log.info("getUserById called for id={}", id);
        Users user  = repo.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        return mapToResponse(user);
    }

    @Override
    public List<UserResponseDto> getAllUsers() {
        log.info("getAllUsers called");
       List<UserResponseDto> users = repo.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        log.info("getAllUsers returned {} users", users.size());
        return users;

    }

    @Override
    @CacheEvict(cacheNames = "users" , key="#id") // expression  language :- #id
    public UserResponseDto updateUser(Long id, UserRequestDto req) {
        log.info("updateUser called for id={}", id);
        Users user  = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("User Not Found"));
        user.setUsername(req.getName());
        user.setEmail(req.getEmail());
        repo.save(user);
        log.info("User updated id={}", id);

        return mapToResponse(user);
    }

    @Override
    @CacheEvict(cacheNames = "users" , key="#id") // expression  language :- #id
    public String deleteUser(Long id) {
        log.info("deleteUser called for id={}", id);
        Users user  = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("User Not Found"));
        repo.deleteById(id);
        log.info("User deleted id={}", id);
        return "User Deleted Success";

    }

    private UserResponseDto mapToResponse(Users user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .name(user.getUsername())
                .email(user.getEmail())
                .build();
    }
}
