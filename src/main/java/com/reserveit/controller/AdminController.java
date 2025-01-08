package com.reserveit.controller;

import com.reserveit.dto.RegisterRequest;
import com.reserveit.dto.UserDto;
import com.reserveit.logic.impl.AuthenticationService;
import com.reserveit.logic.interfaces.AdminService;
import com.reserveit.logic.interfaces.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = { "http://localhost:5200"}, allowCredentials = "true")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@Valid @RequestBody RegisterRequest request) {
        try {
            if ((request.getRole().equalsIgnoreCase("MANAGER") || request.getRole().equalsIgnoreCase("STAFF")) &&
                    request.getCompanyId() == null) {
                return ResponseEntity.badRequest().body("companyId is required for MANAGER and STAFF roles");
            }
            adminService.createUser(request);
            return ResponseEntity.ok().body("User created successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable String id) {
        try {
            adminService.deleteUser(UUID.fromString(id));
            return ResponseEntity.ok().body("User deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to delete user: " + e.getMessage());
        }
    }
}