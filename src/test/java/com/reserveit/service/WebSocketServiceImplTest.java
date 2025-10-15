package service;

import com.reserveit.dto.ReservationDto;
import com.reserveit.dto.TablePositionDto;
import com.reserveit.enums.TableStatus;
import com.reserveit.logic.impl.WebSocketServiceImpl;
import com.reserveit.logic.interfaces.DiningTableService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebSocketServiceImplTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private DiningTableService diningTableService;

    @InjectMocks
    private WebSocketServiceImpl webSocketService;

    private UUID companyId;
    private TablePositionDto tableDto;
    private ReservationDto reservationDto;

    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();
        tableDto = createTestTableDto();
        reservationDto = createTestReservationDto();
    }

    @Test
    void notifyTableUpdate_Success() {
        // Arrange
        Long tableId = 1L;
        String expectedDestination = "/topic/tables/" + companyId;
        when(diningTableService.getTablePosition(tableId)).thenReturn(tableDto);

        // Act
        webSocketService.notifyTableUpdate(tableId);

        // Assert
        verify(messagingTemplate).convertAndSend(eq(expectedDestination), (Object) any(TablePositionDto.class));
    }

    @Test
    void notifyTableUpdate_NullCompanyId() {
        // Arrange
        Long tableId = 1L;
        tableDto.setCompanyId(null);
        when(diningTableService.getTablePosition(tableId)).thenReturn(tableDto);

        // Act
        webSocketService.notifyTableUpdate(tableId);

        // Assert
        verify(messagingTemplate, never()).convertAndSend(Optional.of(anyString()), any());
    }

    @Test
    void notifyTableUpdate_ErrorHandling() {
        // Arrange
        Long tableId = 1L;
        when(diningTableService.getTablePosition(tableId)).thenThrow(new RuntimeException("Database error"));

        // Act
        webSocketService.notifyTableUpdate(tableId);

        // Assert
        verify(messagingTemplate, never()).convertAndSend(Optional.of(anyString()), any());
    }

    @Test
    void sendStaffNotification_Success() {
        // Arrange
        String expectedDestination = "/topic/notifications/" + companyId;
        Map<String, Object> notification = createTestNotification();

        // Act
        webSocketService.sendStaffNotification(companyId, notification);

        // Assert
        verify(messagingTemplate).convertAndSend(eq(expectedDestination), eq(notification));
    }


    @Test
    void sendStaffNotification_NullCompanyId() {
        // Arrange
        Map<String, Object> notification = createTestNotification();

        // Act
        webSocketService.sendStaffNotification(null, notification);

        // Assert
        verify(messagingTemplate, never()).convertAndSend(anyString(), Optional.ofNullable(any()));
    }

    @Test
    void sendStaffNotification_ErrorHandling() {
        // Arrange
        Map<String, Object> notification = createTestNotification();
        String expectedDestination = "/topic/notifications/" + companyId;
        doThrow(new RuntimeException("Messaging error"))
                .when(messagingTemplate).convertAndSend(eq(expectedDestination), eq(notification));

        // Act
        webSocketService.sendStaffNotification(companyId, notification);

        // Assert
        verify(messagingTemplate).convertAndSend(eq(expectedDestination), eq(notification));
    }


    @Test
    void notifyReservationUpdate_Success() {
        // Arrange
        String expectedDestination = "/topic/reservations/" + companyId;

        // Act
        webSocketService.notifyReservationUpdate(reservationDto);

        // Assert
        verify(messagingTemplate).convertAndSend(eq(expectedDestination), eq(reservationDto));
    }


    @Test
    void notifyReservationUpdate_NullCompanyId() {
        // Arrange
        reservationDto.setCompanyId(null);

        // Act
        webSocketService.notifyReservationUpdate(reservationDto);

        // Assert
        verify(messagingTemplate, never()).convertAndSend(Optional.of(anyString()), any());
    }

    @Test
    void notifyReservationUpdate_NullReservation() {
        // Act
        webSocketService.notifyReservationUpdate(null);

        // Assert
        verify(messagingTemplate, never()).convertAndSend(Optional.of(anyString()), any());
    }

    @Test
    void notifyReservationUpdate_ErrorHandling() {
        // Arrange
        String expectedDestination = "/topic/reservations/" + companyId;
        doThrow(new RuntimeException("Messaging error"))
                .when(messagingTemplate).convertAndSend(eq(expectedDestination), eq(reservationDto));

        // Act
        webSocketService.notifyReservationUpdate(reservationDto);

        // Assert
        verify(messagingTemplate).convertAndSend(eq(expectedDestination), eq(reservationDto));
    }


    // Helper methods
    private TablePositionDto createTestTableDto() {
        TablePositionDto dto = new TablePositionDto();
        dto.setId(1L);
        dto.setTableNumber("T1");
        dto.setStatus(TableStatus.AVAILABLE);
        dto.setCompanyId(companyId);
        return dto;
    }

    private ReservationDto createTestReservationDto() {
        ReservationDto dto = new ReservationDto();
        dto.setId(1L);
        dto.setCompanyId(companyId);
        dto.setTableId(1L);
        dto.setReservationDate(LocalDateTime.now().plusHours(1).toString());
        return dto;
    }

    private Map<String, Object> createTestNotification() {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "TABLE_STATUS_CHANGED");
        notification.put("tableId", 1L);
        notification.put("status", "AVAILABLE");
        return notification;
    }
}
