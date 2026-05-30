package com.platform.ecommerce.user.controller;

import com.platform.ecommerce.user.dto.UserRequestDto;
import com.platform.ecommerce.user.dto.UserResponseDto;
import com.platform.ecommerce.user.repository.UserRepository;
import com.platform.ecommerce.user.service.JwtService;
import com.platform.ecommerce.user.service.SessionService;
import com.platform.ecommerce.user.service.UserDetailService;
import com.platform.ecommerce.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserDetailService userDetailService;

    @Autowired
    private SessionService sessionService;


    @PostMapping("/register")
    public UserResponseDto createUser(@Valid @RequestBody UserRequestDto user){
        log.info("HTTP POST /api/users/register called for username={}", user.getName());
        return userService.createUser(user);
    }

    @GetMapping("/{id}")
    public UserResponseDto getUser(@PathVariable Long id){
        log.info("HTTP GET /api/users/{} called", id);
        return userService.getUserById(id);
    }

    @GetMapping("/all")
    public List<UserResponseDto> getAllUser(){
        log.info("HTTP GET /api/users/all called");
        return userService.getAllUsers();
    }

    @PutMapping("/{id}")
    public UserResponseDto updateUser(@PathVariable Long id,
                                      @RequestBody UserRequestDto req){
        log.info("HTTP PUT /api/users/{} called", id);
        return userService.updateUser(id , req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id){
         log.info("HTTP DELETE /api/users/{} called", id);
         String res = userService.deleteUser(id);
         return ResponseEntity.ok(res);
    }

//    @PostMapping("/register")
//    public ResponseEntity<?> registerUser(@RequestBody  UserRequestDto req){
//        try{
//            Users  user = new Users();
//            user.setEmail(req.getEmail());
//            user.setActive(true);
//            user.setUsername(req.getName());
//            BCryptPasswordEncoder brcypt  = new BCryptPasswordEncoder(12);
//            user.setPassword(brcypt.encode(req.getPassword()));
//            userRepo.save(user);
//            return ResponseEntity.ok("User added successfully");
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody UserRequestDto user){
        System.out.println("user " + user);
        String auth = userDetailService.getAuthenticate(user);
        if(auth.equals("Success")){
            String token = jwtService.generateToken(user.getName());
            sessionService.storeSession(user.getName(), token);
            return ResponseEntity.ok("Login Success; Token - "+   token);
        }
        return ResponseEntity.badRequest().build();
    }


}
