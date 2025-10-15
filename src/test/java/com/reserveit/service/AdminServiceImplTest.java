package service;

import com.reserveit.database.interfaces.UserDatabase;
import com.reserveit.dto.RegisterRequest;
import com.reserveit.dto.UserDto;
import com.reserveit.enums.UserRole;
import com.reserveit.logic.impl.AdminServiceImpl;
import com.reserveit.logic.impl.EmailServiceImpl;
import com.reserveit.logic.interfaces.RefreshTokenService;
import com.reserveit.logic.interfaces.StaffService;
import com.reserveit.model.Staff;
import com.reserveit.model.User;
import com.reserveit.util.PasswordGenerator;
import com.reserveit.util.PasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock
    private UserDatabase userDatabase;

    @Mock
    private PasswordHasher passwordHasher;

    @Mock
    private EmailServiceImpl emailService;

    @Mock
    private PasswordGenerator passwordGenerator;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private StaffService staffService;

    @InjectMocks
    private AdminServiceImpl adminService;

    @Nested
    class CreateUserTests {
        @Test
        void createUser_Success_Customer() {
            // Arrange
            RegisterRequest request = createSampleRegisterRequest();
            String generatedPassword = "generated_password";
            User mockUser = createSampleUser();

            when(passwordGenerator.generateSecurePassword()).thenReturn(generatedPassword);
            when(passwordHasher.hashPassword(generatedPassword)).thenReturn("hashed_password");
            when(userDatabase.existsByEmail(request.getEmail())).thenReturn(false);
            when(userDatabase.save(any(User.class))).thenReturn(mockUser);
            doNothing().when(emailService).sendUserCredentials(any(), any(), any());

            // Act
            assertDoesNotThrow(() -> adminService.createUser(request));

            // Assert
            verify(userDatabase).save(any(User.class));
            verify(emailService).sendUserCredentials(
                    eq(request.getEmail()),
                    eq(request.getFirstName() + " " + request.getLastName()),
                    eq(generatedPassword)
            );
        }

        @Test
        void createUser_Success_Manager() {
            // Arrange
            RegisterRequest request = createSampleRegisterRequest();
            request.setRole("MANAGER");
            request.setCompanyId(UUID.randomUUID());
            String generatedPassword = "generated_password";
            Staff mockStaff = createSampleStaff();

            when(passwordGenerator.generateSecurePassword()).thenReturn(generatedPassword);
            when(userDatabase.existsByEmail(request.getEmail())).thenReturn(false);
            when(staffService.createManager(any(), any(), any())).thenReturn(mockStaff);

            // Act
            assertDoesNotThrow(() -> adminService.createUser(request));

            // Assert
            verify(staffService).createManager(any(), eq(request.getCompanyId()), eq(generatedPassword));
            verify(emailService).sendUserCredentials(any(), any(), eq(generatedPassword));
        }

        @Test
        void createUser_EmailAlreadyExists() {
            // Arrange
            RegisterRequest request = createSampleRegisterRequest();
            when(userDatabase.existsByEmail(request.getEmail())).thenReturn(true);

            // Act & Assert
            IllegalStateException exception = assertThrows(IllegalStateException.class,
                    () -> adminService.createUser(request));
            assertEquals("Email already exists", exception.getMessage());
        }

        @Test
        void createUser_InvalidRole() {
            // Arrange
            RegisterRequest request = createSampleRegisterRequest();
            request.setRole("INVALID_ROLE");

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> adminService.createUser(request));
            assertTrue(exception.getMessage().contains("Invalid role"));
        }
    }

    @Nested
    class DeleteUserTests {
        @Test
        void deleteUser_Success() {
            // Arrange
            UUID userId = UUID.randomUUID();
            User mockUser = createSampleUser();
            when(userDatabase.findById(userId)).thenReturn(Optional.of(mockUser));
            doNothing().when(refreshTokenService).deleteAllUserTokens(any());
            doNothing().when(userDatabase).deleteById(any());

            // Act
            assertDoesNotThrow(() -> adminService.deleteUser(userId));

            // Assert
            verify(refreshTokenService).deleteAllUserTokens(mockUser);
            verify(userDatabase).deleteById(userId);
        }

        @Test
        void deleteUser_UserNotFound() {
            // Arrange
            UUID userId = UUID.randomUUID();
            when(userDatabase.findById(userId)).thenReturn(Optional.empty());

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> adminService.deleteUser(userId));
            assertEquals("User not found with id: " + userId, exception.getMessage());
        }
    }

    @Test
    void getAllUsers_Success() {
        // Arrange
        List<User> mockUsers = Arrays.asList(
                createSampleUser(),
                createSampleUser()
        );
        when(userDatabase.findAll()).thenReturn(mockUsers);

        // Act
        List<UserDto> result = adminService.getAllUsers();

        // Assert
        assertEquals(2, result.size());
        verify(userDatabase).findAll();
    }

    // Helper methods
    private RegisterRequest createSampleRegisterRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john@example.com");
        request.setPhoneNumber("1234567890");
        request.setRole("CUSTOMER");
        return request;
    }

    private User createSampleUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john@example.com");
        user.setPhoneNumber("1234567890");
        user.setUserRole(UserRole.CUSTOMER);
        return user;
    }

    private Staff createSampleStaff() {
        Staff staff = new Staff(UserRole.MANAGER);
        staff.setId(UUID.randomUUID());
        staff.setFirstName("John");
        staff.setLastName("Doe");
        staff.setEmail("john@example.com");
        staff.setPhoneNumber("1234567890");
        return staff;
    }

    @Nested
    class ValidationTests {
        @Test
        void createUser_MissingFirstName() {
            // Arrange
            RegisterRequest request = createSampleRegisterRequest();
            request.setFirstName("");

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> adminService.createUser(request));
            assertEquals("First name is required", exception.getMessage());
        }

        @Test
        void createUser_MissingLastName() {
            // Arrange
            RegisterRequest request = createSampleRegisterRequest();
            request.setLastName("");

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> adminService.createUser(request));
            assertEquals("Last name is required", exception.getMessage());
        }

        @Test
        void createUser_MissingEmail() {
            // Arrange
            RegisterRequest request = createSampleRegisterRequest();
            request.setEmail("");

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> adminService.createUser(request));
            assertEquals("Email is required", exception.getMessage());
        }

        @Test
        void createUser_MissingRole() {
            // Arrange
            RegisterRequest request = createSampleRegisterRequest();
            request.setRole(null);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> adminService.createUser(request));
            assertEquals("Role is required", exception.getMessage());
        }

        @Test
        void createUser_ManagerWithoutCompanyId() {
            // Arrange
            RegisterRequest request = createSampleRegisterRequest();
            request.setRole("MANAGER");
            request.setCompanyId(null);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> adminService.createUser(request));
            assertEquals("Company ID is required for manager role", exception.getMessage());
        }
    }
}