package com.reserveit.service;

import com.reserveit.database.interfaces.UserDatabase;
import com.reserveit.dto.UserDto;
import com.reserveit.enums.UserRole;
import com.reserveit.logic.impl.UserServiceImp;
import com.reserveit.logic.interfaces.RefreshTokenService;
import com.reserveit.model.Staff;
import com.reserveit.model.User;
import com.reserveit.util.PasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserDatabase userDb;
    @Mock
    private PasswordHasher passwordHasher;
    @Mock
    private RefreshTokenService refreshTokenService;

    private UserServiceImp userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImp(userDb, passwordHasher, refreshTokenService);
    }

    @Nested
    class CreateUserTests {
        @Test
        void createUser_Success() {
            // Arrange
            UserDto userDto = createSampleUserDto();
            String rawPassword = "password123";
            String hashedPassword = "hashedPassword";
            User savedUser = createSampleUser();

            when(passwordHasher.hashPassword(rawPassword)).thenReturn(hashedPassword);
            when(userDb.save(any(User.class))).thenReturn(savedUser);

            // Act
            UserDto result = userService.createUser(userDto, rawPassword);

            // Assert
            assertNotNull(result);
            assertEquals(userDto.getEmail(), result.getEmail());
            verify(userDb).save(any(User.class));
            verify(passwordHasher).hashPassword(rawPassword);
        }
    }

    @Nested
    class GetUsersTests {
        @Test
        void getAllUsers_Success() {
            // Arrange
            List<User> users = Arrays.asList(
                    createSampleUser(),
                    createSampleUser()
            );
            when(userDb.findAll()).thenReturn(users);

            // Act
            List<UserDto> result = userService.getAllUsers();

            // Assert
            assertEquals(2, result.size());
            verify(userDb).findAll();
        }

        @Test
        void getUserById_Found() {
            // Arrange
            UUID userId = UUID.randomUUID();
            User user = createSampleUser();
            when(userDb.findById(userId)).thenReturn(Optional.of(user));

            // Act
            UserDto result = userService.getUserById(userId);

            // Assert
            assertNotNull(result);
            assertEquals(user.getEmail(), result.getEmail());
        }

        @Test
        void getUserById_NotFound() {
            // Arrange
            UUID userId = UUID.randomUUID();
            when(userDb.findById(userId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> userService.getUserById(userId));
        }

        @Test
        void getUserByEmail_Found() {
            // Arrange
            String email = "test@example.com";
            User user = createSampleUser();
            when(userDb.findByEmail(email)).thenReturn(user);

            // Act
            UserDto result = userService.getUserByEmail(email);

            // Assert
            assertNotNull(result);
            assertEquals(email, result.getEmail());
        }

        @Test
        void getUserByEmail_NotFound() {
            // Arrange
            String email = "nonexistent@example.com";
            when(userDb.findByEmail(email)).thenReturn(null);

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> userService.getUserByEmail(email));
        }
    }
    @Nested
    class UpdatePasswordTests {
        @Test
        void updatePassword_ProfileSetUp_Success() {
            // Arrange
            UUID userId = UUID.randomUUID();
            User user = createSampleUser();
            String oldPassword = "oldPass";
            String newPassword = "newPass";
            String newHashedPassword = "newHashedPass";

            when(userDb.findById(userId)).thenReturn(Optional.of(user));
            when(passwordHasher.matches(oldPassword, user.getHashedPassword())).thenReturn(true);
            when(passwordHasher.hashPassword(newPassword)).thenReturn(newHashedPassword);

            // Act
            int result = userService.updatePassword(userId, oldPassword, newPassword);

            // Assert
            assertEquals(0, result); // Profile is set up
            assertEquals(newHashedPassword, user.getHashedPassword());
            verify(userDb).save(user);
        }

        @Test
        void updatePassword_PasswordsDoNotMatch() {
            // Arrange
            UUID userId = UUID.randomUUID();
            User user = createSampleUser();
            String oldPassword = "wrongOldPass";
            String newPassword = "newPass";

            when(userDb.findById(userId)).thenReturn(Optional.of(user));
            when(passwordHasher.matches(anyString(), anyString())).thenReturn(false);

            // Act
            int result = userService.updatePassword(userId, oldPassword, newPassword);

            // Assert
            assertEquals(1, result); // Passwords don't match
            verify(userDb, never()).save(any());
        }

        @Test
        void updatePassword_ProfileNotFound() {
            // Arrange
            UUID userId = UUID.randomUUID();
            String oldPassword = "oldPass";
            String newPassword = "newPass";

            when(userDb.findById(userId)).thenReturn(Optional.empty());

            // Act
            int result = userService.updatePassword(userId, oldPassword, newPassword);

            // Assert
            assertEquals(2, result); // Profile not found
            verify(userDb, never()).save(any());
        }
    }

    @Nested
    class GetUserEntityByEmailTests {
        @Test
        void getUserEntityByEmail_Found() {
            // Arrange
            String email = "test@example.com";
            User user = createSampleUser();
            when(userDb.findByEmail(email)).thenReturn(user);

            // Act
            User result = userService.getUserEntityByEmail(email);

            // Assert
            assertNotNull(result);
            assertEquals(email, result.getEmail());
            verify(userDb).findByEmail(email);
        }

        @Test
        void getUserEntityByEmail_NotFound() {
            // Arrange
            String email = "nonexistent@example.com";
            when(userDb.findByEmail(email)).thenReturn(null);

            // Act & Assert
            Exception exception = assertThrows(IllegalArgumentException.class,
                    () -> userService.getUserEntityByEmail(email));
            assertEquals("User not found with email: " + email, exception.getMessage());
            verify(userDb).findByEmail(email);
        }
    }

    @Nested
    class UpdateUserTests {
        @Test
        void updateUserDetails_Success() {
            // Arrange
            UUID userId = UUID.randomUUID();
            User existingUser = createSampleUser();
            when(userDb.findById(userId)).thenReturn(Optional.of(existingUser));

            // Act
            boolean result = userService.updateUserDetails(userId, "new@example.com", "1234567890");

            // Assert
            assertTrue(result);
            assertEquals("new@example.com", existingUser.getEmail());
            assertEquals("1234567890", existingUser.getPhoneNumber());
        }

        @Test
        void updateUserDetails_UserNotFound() {
            // Arrange
            UUID userId = UUID.randomUUID();
            when(userDb.findById(userId)).thenReturn(Optional.empty());

            // Act
            boolean result = userService.updateUserDetails(userId, "new@example.com", "1234567890");

            // Assert
            assertFalse(result);
        }

        @Test
        void updatePassword_Success() {
            // Arrange
            UUID userId = UUID.randomUUID();
            User user = createSampleUser();
            String oldPassword = "oldPass";
            String newPassword = "newPass";
            String newHashedPassword = "newHashedPass";

            when(userDb.findById(userId)).thenReturn(Optional.of(user));
            when(passwordHasher.matches(oldPassword, user.getHashedPassword())).thenReturn(true);
            when(passwordHasher.hashPassword(newPassword)).thenReturn(newHashedPassword);

            // Act
            int result = userService.updatePassword(userId, oldPassword, newPassword);

            // Assert
            assertEquals(0, result);
            assertEquals(newHashedPassword, user.getHashedPassword());
            verify(userDb).save(user);
        }

        @Test
        void updatePassword_WrongOldPassword() {
            // Arrange
            UUID userId = UUID.randomUUID();
            User user = createSampleUser();
            when(userDb.findById(userId)).thenReturn(Optional.of(user));
            when(passwordHasher.matches(anyString(), anyString())).thenReturn(false);

            // Act
            int result = userService.updatePassword(userId, "wrongPass", "newPass");

            // Assert
            assertEquals(1, result);
            verify(userDb, never()).save(any());
        }
    }

    @Nested
    class DeleteUserTests {
        @Test
        void deleteUser_Success() {
            // Arrange
            UUID userId = UUID.randomUUID();
            User user = createSampleUser();
            when(userDb.findById(userId)).thenReturn(Optional.of(user));

            // Act
            userService.deleteUserById(userId);

            // Assert
            verify(refreshTokenService).deleteAllUserTokens(user);
            verify(userDb).deleteById(userId);
        }

        @Test
        void deleteUser_StaffMember() {
            // Arrange
            UUID userId = UUID.randomUUID();
            Staff staffMember = createSampleStaffMember();
            when(userDb.findById(userId)).thenReturn(Optional.of(staffMember));

            // Act
            userService.deleteUserById(userId);

            // Assert
            verify(refreshTokenService).deleteAllUserTokens(staffMember);
            verify(userDb).deleteById(userId);
            // Remove the verification of save() since it's not needed
            assertNull(staffMember.getCompany());
        }
    }

    // Helper methods
    private UserDto createSampleUserDto() {
        UserDto dto = new UserDto();
        dto.setId(UUID.randomUUID());
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setEmail("test@example.com");
        dto.setPhoneNumber("1234567890");
        dto.setRole(UserRole.CUSTOMER);
        return dto;
    }

    private User createSampleUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("test@example.com");
        user.setPhoneNumber("1234567890");
        user.setUserRole(UserRole.CUSTOMER);
        user.setHashedPassword("hashedPassword");
        return user;
    }

    private Staff createSampleStaffMember() {
        Staff staff = new Staff(UserRole.STAFF);
        staff.setId(UUID.randomUUID());
        staff.setFirstName("John");
        staff.setLastName("Doe");
        staff.setEmail("test@example.com");
        staff.setPhoneNumber("1234567890");
        staff.setHashedPassword("hashedPassword");
        return staff;
    }

}