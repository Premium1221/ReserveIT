package service;

import com.reserveit.logic.impl.UserServiceImp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.reserveit.database.interfaces.UserDatabase;
import com.reserveit.dto.UserDto;
import com.reserveit.model.User;
import com.reserveit.logic.impl.EmailServiceImpl;
import com.reserveit.util.PasswordHasher;
import com.reserveit.enums.UserRole;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    @Mock
    private UserDatabase userDb;

    @Mock
    private PasswordHasher passwordHasher;

    private UserServiceImp userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserServiceImp(userDb, passwordHasher);
    }

    @Test
    void createUser_ValidData_Success() {
        // Arrange
        UserDto dto = new UserDto();
        dto.setEmail("test@test.com");
        dto.setFirstName("Test");
        dto.setLastName("User");
        dto.setRole(UserRole.CUSTOMER);

        String rawPassword = "password123";
        String hashedPassword = "hashedPassword";

        when(passwordHasher.hashPassword(rawPassword)).thenReturn(hashedPassword);
        when(userDb.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        UserDto result = userService.createUser(dto, rawPassword);

        // Assert
        assertNotNull(result);
        assertEquals(dto.getEmail(), result.getEmail());
        assertEquals(dto.getRole(), result.getRole());
    }

    @Test
    void getAllUsers_ReturnsCorrectList() {
        // Arrange
        User user1 = new User();
        user1.setEmail("user1@test.com");
        User user2 = new User();
        user2.setEmail("user2@test.com");

        when(userDb.findAll()).thenReturn(Arrays.asList(user1, user2));

        // Act
        List<UserDto> results = userService.getAllUsers();

        // Assert
        assertEquals(2, results.size());
    }

    @Test
    void getUserById_ValidId_ReturnsUser() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setEmail("test@test.com");

        when(userDb.findById(userId)).thenReturn(Optional.of(mockUser));

        // Act
        UserDto result = userService.getUserById(userId);

        // Assert
        assertNotNull(result);
        assertEquals(mockUser.getEmail(), result.getEmail());
    }

    @Test
    void getUserById_InvalidId_ThrowsException() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(userDb.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userService.getUserById(nonExistentId);
        });
    }

    @Test
    void updatePassword_ValidData_Success() {
        // Arrange
        UUID userId = UUID.randomUUID();
        String oldPassword = "oldPassword";
        String newPassword = "newPassword";

        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setHashedPassword("hashedOldPassword");

        when(userDb.findById(userId)).thenReturn(Optional.of(mockUser));
        when(passwordHasher.matches(oldPassword, mockUser.getHashedPassword())).thenReturn(true);

        // Act
        int result = userService.updatePassword(userId, oldPassword, newPassword);

        // Assert
        assertEquals(0, result); // 0 indicates success
    }
}
