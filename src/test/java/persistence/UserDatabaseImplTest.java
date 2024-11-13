package persistence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.reserveit.model.User;
import com.reserveit.repository.UserRepository;
import com.reserveit.database.impl.UserDatabaseImpl;
import com.reserveit.enums.UserRole;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserDatabaseImplTest {

    @Mock
    private UserRepository userRepository;

    private UserDatabaseImpl userDatabase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userDatabase = new UserDatabaseImpl(userRepository);
    }

    @Test
    void findByEmail_ShouldReturnUser_WhenExists() {
        // Arrange
        String email = "test@example.com";
        User expectedUser = new User();
        expectedUser.setEmail(email);
        when(userRepository.findByEmail(email)).thenReturn(expectedUser);

        // Act
        User actualUser = userDatabase.findByEmail(email);

        // Assert
        assertNotNull(actualUser);
        assertEquals(email, actualUser.getEmail());
        verify(userRepository).findByEmail(email);
    }

    @Test
    void findByEmail_ShouldReturnNull_WhenUserNotFound() {
        // Arrange
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(null);

        // Act
        User user = userDatabase.findByEmail(email);

        // Assert
        assertNull(user);
        verify(userRepository).findByEmail(email);
    }

    @Test
    void save_ShouldReturnSavedUser() {
        // Arrange
        User user = new User();
        user.setEmail("test@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setUserRole(UserRole.CUSTOMER);

        when(userRepository.save(user)).thenReturn(user);

        // Act
        User savedUser = userDatabase.save(user);

        // Assert
        assertEquals(user, savedUser);
        verify(userRepository).save(user);
    }

    @Test
    void findAll_ShouldReturnAllUsers() {
        // Arrange
        List<User> expectedUsers = Arrays.asList(
                createTestUser("user1@example.com", UserRole.CUSTOMER),
                createTestUser("user2@example.com", UserRole.STAFF)
        );
        when(userRepository.findAll()).thenReturn(expectedUsers);

        // Act
        List<User> actualUsers = userDatabase.findAll();

        // Assert
        assertEquals(expectedUsers, actualUsers);
        assertEquals(2, actualUsers.size());
        verify(userRepository).findAll();
    }

    @Test
    void findById_ShouldReturnUser_WhenExists() {
        // Arrange
        UUID id = UUID.randomUUID();
        User expectedUser = createTestUser("test@example.com", UserRole.CUSTOMER);
        expectedUser.setId(id);

        when(userRepository.findById(id)).thenReturn(Optional.of(expectedUser));

        // Act
        Optional<User> actualUser = userDatabase.findById(id);

        // Assert
        assertTrue(actualUser.isPresent());
        assertEquals(id, actualUser.get().getId());
        verify(userRepository).findById(id);
    }

    @Test
    void findById_ShouldReturnEmpty_WhenUserNotFound() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        Optional<User> user = userDatabase.findById(id);

        // Assert
        assertTrue(user.isEmpty());
        verify(userRepository).findById(id);
    }

    @Test
    void deleteById_ShouldCallRepositoryDelete() {
        // Arrange
        UUID id = UUID.randomUUID();

        // Act
        userDatabase.deleteById(id);

        // Assert
        verify(userRepository).deleteById(id);
    }

    // Helper method to create test users
    private User createTestUser(String email, UserRole role) {
        User user = new User();
        user.setEmail(email);
        user.setUserRole(role);
        user.setFirstName("Test");
        user.setLastName("User");
        return user;
    }
}
