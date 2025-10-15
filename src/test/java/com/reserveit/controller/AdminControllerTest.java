package com.reserveit.controller;

import com.reserveit.controller.AdminController;
import com.reserveit.dto.RegisterRequest;
import com.reserveit.dto.UserDto;
import com.reserveit.logic.interfaces.AdminService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import static org.junit.jupiter.api.Assertions.assertEquals;


import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {
    @Mock
    private AdminService adminService;

    @InjectMocks
    private AdminController adminController;

    @Test
    void createUser_WithValidData_ReturnsSuccess() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setRole("CUSTOMER");
        request.setPhoneNumber("1234567890");

        doNothing().when(adminService).createUser(request);

        // Act
        ResponseEntity<?> response = adminController.createUser(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User created successfully", response.getBody());
        verify(adminService).createUser(request);
    }

    @Test
    void createUser_StaffWithoutCompanyId_ReturnsBadRequest() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("staff@example.com");
        request.setRole("STAFF");
        request.setCompanyId(null);

        // Act
        ResponseEntity<?> response = adminController.createUser(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("companyId is required for MANAGER and STAFF roles", response.getBody());
        verify(adminService, never()).createUser(request);
    }

    @Test
    void createUser_ServiceThrowsException_ReturnsBadRequest() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setRole("CUSTOMER");

        doThrow(new IllegalArgumentException("Invalid data")).when(adminService).createUser(request);

        // Act
        ResponseEntity<?> response = adminController.createUser(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid data", response.getBody());
    }

    @Test
    void getAllUsers_ReturnsUsersList() {
        // Arrange
        List<UserDto> users = Arrays.asList(
                createUserDto("user1@example.com"),
                createUserDto("user2@example.com")
        );
        when(adminService.getAllUsers()).thenReturn(users);

        // Act
        ResponseEntity<List<UserDto>> response = adminController.getAllUsers();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void deleteUser_Success() {
        // Arrange
        String userId = UUID.randomUUID().toString();
        doNothing().when(adminService).deleteUser(UUID.fromString(userId));

        // Act
        ResponseEntity<?> response = adminController.deleteUser(userId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User deleted successfully", response.getBody());
    }

    @Test
    void deleteUser_WithInvalidUUID_ReturnsBadRequest() {
        // Arrange
        String invalidUserId = "invalid-uuid";

        // Act
        ResponseEntity<?> response = adminController.deleteUser(invalidUserId);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Failed to delete user"));
    }

    private UserDto createUserDto(String email) {
        UserDto dto = new UserDto();
        dto.setId(UUID.randomUUID());
        dto.setEmail(email);
        dto.setFirstName("Test");
        dto.setLastName("User");
        return dto;
    }
}
