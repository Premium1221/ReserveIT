package com.reserveit.controller;

import com.reserveit.controller.UserController;
import com.reserveit.dto.UserDto;
import com.reserveit.enums.UserRole;
import com.reserveit.logic.interfaces.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private UUID testUserId;
    private UserDto testUserDto;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUserDto = createTestUserDto();
    }

    @Nested
    class GetUserTests {
        @Test
        void getUserById_Success() {
            // Arrange
            when(userService.getUserById(testUserId)).thenReturn(testUserDto);

            // Act
            ResponseEntity<UserDto> response = userController.getUserById(testUserId);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(testUserDto, response.getBody());
            verify(userService).getUserById(testUserId);
        }

        @Test
        void getUserById_NotFound() {
            // Arrange
            when(userService.getUserById(testUserId))
                    .thenThrow(new IllegalArgumentException("User not found"));

            // Act
            ResponseEntity<UserDto> response = userController.getUserById(testUserId);

            // Assert
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertNull(response.getBody());
        }


        @Test
        void getUserByEmail_Success() {
            // Arrange
            String email = "test@example.com";
            when(userService.getUserByEmail(email)).thenReturn(testUserDto);

            // Act
            ResponseEntity<UserDto> response = userController.getUserByEmail(email);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(testUserDto, response.getBody());
            verify(userService).getUserByEmail(email);
        }

        @Test
        void getUserByEmail_NotFound() {
            // Arrange
            String email = "nonexistent@example.com";
            when(userService.getUserByEmail(email))
                    .thenThrow(new IllegalArgumentException("User not found"));

            // Act
            ResponseEntity<UserDto> response = userController.getUserByEmail(email);

            // Assert
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertNull(response.getBody());
        }
    }

    @Nested
    class GetAllUsersTests {
        @Test
        void getAllUsers_Success() {
            // Arrange
            List<UserDto> users = Arrays.asList(
                    createTestUserDto(),
                    createTestUserDto()
            );
            when(userService.getAllUsers()).thenReturn(users);

            // Act
            ResponseEntity<List<UserDto>> response = userController.getAllUsers();

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(2, response.getBody().size());
            verify(userService).getAllUsers();
        }

        @Test
        void getAllUsers_EmptyList() {
            // Arrange
            when(userService.getAllUsers()).thenReturn(Arrays.asList());

            // Act
            ResponseEntity<List<UserDto>> response = userController.getAllUsers();

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().isEmpty());
        }
    }

    @Nested
    class DeleteUserTests {
        @Test
        void deleteUserById_Success() {
            // Arrange
            doNothing().when(userService).deleteUserById(testUserId);

            // Act
            ResponseEntity<Void> response = userController.deleteUserById(testUserId);

            // Assert
            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
            verify(userService).deleteUserById(testUserId);
        }

        @Test
        void deleteUserById_NotFound() {
            // Arrange
            doThrow(new IllegalArgumentException("User not found"))
                    .when(userService).deleteUserById(testUserId);

            // Act
            ResponseEntity<Void> response = userController.deleteUserById(testUserId);

            // Assert
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }

        @Test
        void deleteUserById_InternalError() {
            // Arrange
            doThrow(new RuntimeException("Internal error"))
                    .when(userService).deleteUserById(testUserId);

            // Act
            ResponseEntity<Void> response = userController.deleteUserById(testUserId);

            // Assert
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            verify(userService).deleteUserById(testUserId);
        }
    }


    // Helper method to create test data
    private UserDto createTestUserDto() {
        UserDto dto = new UserDto();
        dto.setId(testUserId);
        dto.setEmail("test@example.com");
        dto.setFirstName("Test");
        dto.setLastName("User");
        dto.setPhoneNumber("1234567890");
        dto.setRole(UserRole.CUSTOMER);
        dto.setCompanyId(UUID.randomUUID()); // Optional, depending on the user type
        return dto;
    }
}