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
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserRepository repo;

    @Override
    @CacheEvict(cacheNames = "users" , key="#result.id") // expression  language :- #id
    public UserResponseDto createUser(UserRequestDto req) {
        Optional<Users> usr = repo.findByEmail(req.getEmail());
        if(usr.isPresent()){
            throw new RuntimeException("Data Already Exist");
        }

        Users users  = Users.builder()
                .name(req.getName())
                .email(req.getEmail())
                .password(req.getPassword())
                .isActive(true)
                .build();
        repo.save(users);
        return mapToResponse(users);

    }

    @Override
    @Cacheable(cacheNames = "users" , key="#id") // expression  language :- #id
    public UserResponseDto getUserById(Long id) {
        Users user  = repo.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        return mapToResponse(user);
    }

    @Override
    public List<UserResponseDto> getAllUsers() {

       return  repo.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

    }

    @Override
    @CacheEvict(cacheNames = "users" , key="#id") // expression  language :- #id
    public UserResponseDto updateUser(Long id, UserRequestDto req) {
        Users user  = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("User Not Found"));
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        repo.save(user);

        return mapToResponse(user);
    }

    @Override
    @CacheEvict(cacheNames = "users" , key="#id") // expression  language :- #id
    public String deleteUser(Long id) {

        Users user  = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("User Not Found"));
        repo.deleteById(id);
        return "User Deleted Success";

    }

    private UserResponseDto mapToResponse(Users user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }
}
