package controller;

import com.reserveit.controller.StaffReservationController;
import com.reserveit.dto.ReservationDto;
import com.reserveit.enums.ReservationStatus;
import com.reserveit.logic.impl.WebSocketServiceImpl;
import com.reserveit.logic.interfaces.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class StaffReservationControllerTest {

    @Mock
    private ReservationService reservationService;

    @Mock
    private WebSocketServiceImpl webSocketService;

    @InjectMocks
    private StaffReservationController staffReservationController;

    private UUID companyId;
    private ReservationDto testReservation;

    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();
        testReservation = createTestReservation();
    }

    @Nested
    class CheckInTests {
        @Test
        void checkInReservation_Success() {
            // Arrange
            Long reservationId = 1L;
            ReservationDto checkedInReservation = createTestReservation();
            checkedInReservation.setStatus(ReservationStatus.ARRIVED);

            when(reservationService.checkInReservation(reservationId)).thenReturn(checkedInReservation);

            // Act
            ResponseEntity<?> response = staffReservationController.checkInReservation(reservationId);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            verify(webSocketService).notifyTableUpdate(checkedInReservation.getTableId());
        }

        @Test
        void checkInReservation_NullId() {
            // Act
            ResponseEntity<?> response = staffReservationController.checkInReservation(null);

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertEquals("Reservation ID is required", response.getBody());
        }

        @Test
        void checkInReservation_ServiceError() {
            // Arrange
            Long reservationId = 1L;
            when(reservationService.checkInReservation(reservationId))
                    .thenThrow(new IllegalArgumentException("Invalid reservation"));

            // Act
            ResponseEntity<?> response = staffReservationController.checkInReservation(reservationId);

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertEquals("Invalid reservation", response.getBody());
        }
    }

    @Nested
    class CheckOutTests {
        @Test
        void checkOutReservation_Success() {
            // Arrange
            Long reservationId = 1L;
            ReservationDto checkedOutReservation = createTestReservation();
            checkedOutReservation.setStatus(ReservationStatus.COMPLETED);

            when(reservationService.checkOutReservation(reservationId)).thenReturn(checkedOutReservation);

            // Act
            ResponseEntity<?> response = staffReservationController.checkOutReservation(reservationId);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            verify(webSocketService).notifyTableUpdate(checkedOutReservation.getTableId());
            verify(webSocketService).sendStaffNotification(eq(checkedOutReservation.getCompanyId()), any());
        }

        @Test
        void checkOutReservation_Error() {
            // Arrange
            Long reservationId = 1L;
            when(reservationService.checkOutReservation(reservationId))
                    .thenThrow(new IllegalStateException("Cannot check out"));

            // Act
            ResponseEntity<?> response = staffReservationController.checkOutReservation(reservationId);

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertEquals("Cannot check out", response.getBody());
        }
    }

    @Nested
    class ReservationRetrievalTests {
        @Test
        void getUpcomingReservations_Success() {
            // Arrange
            List<ReservationDto> upcomingReservations = Arrays.asList(
                    createTestReservation(),
                    createTestReservation()
            );
            when(reservationService.getReservationsByTimeRange(any(), any()))
                    .thenReturn(upcomingReservations);

            // Act
            ResponseEntity<List<ReservationDto>> response = staffReservationController.getUpcomingReservations();

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(2, response.getBody().size());
        }

        @Test
        void getReservationsByStatus_Success() {
            // Arrange
            List<ReservationDto> reservations = Arrays.asList(
                    createTestReservation(),
                    createTestReservation()
            );
            when(reservationService.findByStatus(any(ReservationStatus.class)))
                    .thenReturn(reservations);

            // Act
            ResponseEntity<List<ReservationDto>> response =
                    staffReservationController.getReservationsByStatus("CONFIRMED");

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(2, response.getBody().size());
        }

        @Test
        void getReservationsByStatus_InvalidStatus() {
            // Act
            ResponseEntity<List<ReservationDto>> response =
                    staffReservationController.getReservationsByStatus("INVALID_STATUS");

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        void getReservationsByCompany_Success() {
            // Arrange
            List<ReservationDto> reservations = Arrays.asList(
                    createTestReservation(),
                    createTestReservation()
            );
            when(reservationService.getReservationsByCompany(companyId))
                    .thenReturn(reservations);

            // Act
            ResponseEntity<List<ReservationDto>> response =
                    staffReservationController.getReservationsByCompany(companyId);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(2, response.getBody().size());
        }
    }

    @Nested
    class NoShowAndMonitoringTests {
        @Test
        void markAsNoShow_Success() {
            // Arrange
            Long reservationId = 1L;
            ReservationDto noShowReservation = createTestReservation();
            noShowReservation.setStatus(ReservationStatus.NO_SHOW);

            when(reservationService.markAsNoShow(reservationId)).thenReturn(noShowReservation);

            // Act
            ResponseEntity<?> response = staffReservationController.markAsNoShow(reservationId);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            verify(webSocketService).notifyTableUpdate(noShowReservation.getTableId());
            verify(webSocketService).sendStaffNotification(eq(noShowReservation.getCompanyId()), any());
        }

        @Test
        void markAsNoShow_Error() {
            // Arrange
            Long reservationId = 1L;
            when(reservationService.markAsNoShow(reservationId))
                    .thenThrow(new IllegalStateException("Cannot mark as no-show"));

            // Act
            ResponseEntity<?> response = staffReservationController.markAsNoShow(reservationId);

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertEquals("Cannot mark as no-show", response.getBody());
        }

        @Test
        void getPendingArrivalReservations_Success() {
            // Arrange
            List<ReservationDto> pendingReservations = Arrays.asList(
                    createTestReservation(),
                    createTestReservation()
            );

            when(reservationService.getReservationsForArrivalCheck(
                    any(UUID.class),
                    any(LocalDateTime.class),
                    any(LocalDateTime.class)
            )).thenReturn(pendingReservations);

            // Act
            ResponseEntity<List<ReservationDto>> response =
                    staffReservationController.getPendingArrivalReservations(companyId);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(2, response.getBody().size());
        }

        @Test
        void getExtendedStayReservations_Success() {
            // Arrange
            List<ReservationDto> extendedStayReservations = Arrays.asList(
                    createTestReservation(),
                    createTestReservation()
            );

            when(reservationService.getExtendedStayReservations(companyId))
                    .thenReturn(extendedStayReservations);

            // Act
            ResponseEntity<List<ReservationDto>> response =
                    staffReservationController.getExtendedStayReservations(companyId);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(2, response.getBody().size());
        }
    }

    // Helper method
    private ReservationDto createTestReservation() {
        ReservationDto dto = new ReservationDto();
        dto.setId(1L);
        dto.setCompanyId(companyId);
        dto.setTableId(1L);
        dto.setStatus(ReservationStatus.CONFIRMED);
        dto.setReservationDate(LocalDateTime.now().plusHours(1).toString());
        dto.setNumberOfPeople(4);
        dto.setDurationMinutes(120);
        return dto;
    }
}