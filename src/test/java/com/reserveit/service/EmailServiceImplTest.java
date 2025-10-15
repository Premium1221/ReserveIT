package service;

import com.reserveit.logic.impl.EmailServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailServiceImpl(mailSender);
    }

    @Test
    void sendUserCredentials_Success() {
        // Arrange
        String toEmail = "test@example.com";
        String fullName = "John Doe";
        String password = "password123";

        ArgumentCaptor<SimpleMailMessage> messageCaptor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Act
        emailService.sendUserCredentials(toEmail, fullName, password);

        // Assert
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();

        assertEquals("areserveit@gmail.com", sentMessage.getFrom());
        assertEquals(toEmail, sentMessage.getTo()[0]);
        assertEquals("Your ReserveIT Account Credentials", sentMessage.getSubject());
        assertTrue(sentMessage.getText().contains(fullName));
        assertTrue(sentMessage.getText().contains(toEmail));
        assertTrue(sentMessage.getText().contains(password));
    }

    @Test
    void sendAdminNotification_Success() {
        // Arrange
        String adminEmail = "admin@example.com";
        String createdUserName = "John Doe";
        String createdUserEmail = "john.doe@example.com";

        ArgumentCaptor<SimpleMailMessage> messageCaptor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Act
        emailService.sendAdminNotification(adminEmail, createdUserName, createdUserEmail);

        // Assert
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();

        assertEquals("areserveit@gmail.com", sentMessage.getFrom());
        assertEquals(adminEmail, sentMessage.getTo()[0]);
        assertEquals("New User Account Created", sentMessage.getSubject());
        assertTrue(sentMessage.getText().contains(createdUserName));
        assertTrue(sentMessage.getText().contains(createdUserEmail));
    }

    @Test
    void sendPasswordReset_Success() {
        // Arrange
        String toEmail = "test@example.com";
        String resetToken = "reset-token-123";

        ArgumentCaptor<SimpleMailMessage> messageCaptor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Act
        emailService.sendPasswordReset(toEmail, resetToken);

        // Assert
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();

        assertEquals("areserveit@gmail.com", sentMessage.getFrom());
        assertEquals(toEmail, sentMessage.getTo()[0]);
        assertEquals("ReserveIT Password Reset", sentMessage.getSubject());
        assertTrue(sentMessage.getText().contains(resetToken));
    }

    @Test
    void sendUserCredentials_HandlesNullValues() {
        // Arrange
        String toEmail = "test@example.com";
        String fullName = null;
        String password = "password123";

        // Act & Assert
        assertDoesNotThrow(() ->
                emailService.sendUserCredentials(toEmail, fullName, password));
    }

    @Test
    void sendAdminNotification_HandlesNullValues() {
        // Arrange
        String adminEmail = "admin@example.com";
        String createdUserName = null;
        String createdUserEmail = "john.doe@example.com";

        // Act & Assert
        assertDoesNotThrow(() ->
                emailService.sendAdminNotification(adminEmail, createdUserName, createdUserEmail));
    }

    @Test
    void mailSender_ThrowsException() {
        // Arrange
        String toEmail = "test@example.com";
        String resetToken = "reset-token-123";
        doThrow(new RuntimeException("Mail server error"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                emailService.sendPasswordReset(toEmail, resetToken));
    }
}