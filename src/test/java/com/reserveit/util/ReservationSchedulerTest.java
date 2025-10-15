package util;

import com.reserveit.dto.ReservationDto;
import com.reserveit.enums.ReservationStatus;
import com.reserveit.enums.TableStatus;
import com.reserveit.logic.interfaces.DiningTableService;
import com.reserveit.logic.interfaces.ReservationService;
import com.reserveit.logic.interfaces.WebSocketService;
import com.reserveit.model.DiningTable;
import com.reserveit.util.ReservationScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ReservationSchedulerTest {

    @Mock
    private ReservationService reservationService;

    @Mock
    private WebSocketService webSocketService;

    @Mock
    private DiningTableService tableService;

    @InjectMocks
    private ReservationScheduler reservationScheduler;

    private UUID companyId;
    private DiningTable testTable;

    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();
        testTable = createTestTable();
    }

    @Test
    void checkAndNotifyLateArrivals_WithLateReservations() {
        // Arrange
        List<Map<String, Object>> notifications = Arrays.asList(
                createNotification("LATE_ARRIVAL", 1L),
                createNotification("LATE_ARRIVAL", 2L)
        );
        when(reservationService.checkForLateArrivals()).thenReturn(notifications);

        // Act
        reservationScheduler.checkAndNotifyLateArrivals();

        // Assert
        verify(webSocketService, times(2)).sendStaffNotification(any(), any());
    }

    @Test
    void checkAndNotifyLateArrivals_NoLateReservations() {
        // Arrange
        when(reservationService.checkForLateArrivals()).thenReturn(Collections.emptyList());

        // Act
        reservationScheduler.checkAndNotifyLateArrivals();

        // Assert
        verify(webSocketService, never()).sendStaffNotification(any(), any());
    }

    @Test
    void checkAndUpdateReservations_WithUpcomingReservations() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        ReservationDto reservation = createReservationDto(ReservationStatus.CONFIRMED);
        reservation.setTableId(1L);
        List<ReservationDto> upcomingReservations = Collections.singletonList(reservation);

        when(reservationService.getReservationsByTimeRange(any(), any())).thenReturn(upcomingReservations);
        when(tableService.findById(1L)).thenReturn(testTable);

        // Act
        reservationScheduler.checkAndUpdateReservations();

        // Assert
        verify(tableService).updateTableStatus(eq(1L), eq(TableStatus.RESERVED));
        verify(webSocketService).sendStaffNotification(any(), any());
    }


    @Test
    void checkAndUpdateReservations_NoUpcomingReservations() {
        // Arrange
        when(reservationService.getReservationsByTimeRange(any(), any()))
                .thenReturn(Collections.emptyList());

        // Act
        reservationScheduler.checkAndUpdateReservations();

        // Assert
        verify(tableService, never()).updateTableStatus(any(Long.class), any());
        verify(webSocketService, never()).sendStaffNotification(any(), any());
    }

    @Test
    void checkAndUpdateReservations_WithActiveReservation() {
        // Arrange
        ReservationDto reservation = createReservationDto(ReservationStatus.CONFIRMED); // A confirmed reservation
        List<ReservationDto> upcomingReservations = Collections.singletonList(reservation);

        when(reservationService.getReservationsByTimeRange(any(), any())).thenReturn(upcomingReservations);

        testTable.setStatus(TableStatus.OCCUPIED);

        // Act
        reservationScheduler.checkAndUpdateReservations();

        // Assert
        verify(tableService, never()).updateTableStatus(any(Long.class), any());
    }

    @Test
    void checkAndUpdateReservations_ErrorHandling() {
        // Arrange
        ReservationDto reservation = createReservationDto(ReservationStatus.CONFIRMED);
        List<ReservationDto> upcomingReservations = Collections.singletonList(reservation);

        when(reservationService.getReservationsByTimeRange(any(), any())).thenReturn(upcomingReservations);
        when(tableService.findById(any())).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertDoesNotThrow(() -> reservationScheduler.checkAndUpdateReservations());
    }

    // Helper methods
    private Map<String, Object> createNotification(String type, Long reservationId) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", type);
        notification.put("reservationId", reservationId);
        notification.put("message", "Late arrival notification");
        notification.put("companyId", companyId);
        return notification;
    }

    private DiningTable createTestTable() {
        DiningTable table = new DiningTable();
        table.setId(1L);
        table.setStatus(TableStatus.AVAILABLE);
        table.setTableNumber("T1");
        table.setCapacity(4);
        return table;
    }

    private ReservationDto createReservationDto(ReservationStatus status) {
        ReservationDto dto = new ReservationDto();
        dto.setId(1L);
        dto.setTableId(1L);
        dto.setCompanyId(companyId);
        dto.setStatus(status);
        dto.setReservationDate(LocalDateTime.now().plusMinutes(30).toString());
        return dto;
    }
}