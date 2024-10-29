package com.reserveit.controller;

import com.reserveit.dto.UserDto;
import com.reserveit.service.UserService;
import com.reserveit.util.PasswordHasher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final PasswordHasher passwordHasher;

    // Inject PasswordHasher through constructor
    public UserController(UserService userService, PasswordHasher passwordHasher) {
        this.userService = userService;
        this.passwordHasher = passwordHasher;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody UserDto userDto, @RequestParam String password) {
        userService.createUser(userDto, password);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestParam String email, @RequestParam String password) {
        UserDto user = userService.getUserByEmail(email);
        if (user != null && passwordHasher.matches(password, user.getHashedPassword())) {
            return ResponseEntity.ok("Login successful");
        }
        return ResponseEntity.status(401).body("Invalid credentials");
    }
}
