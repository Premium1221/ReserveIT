package com.reserveit.controller;

import com.reserveit.controller.ReservationController;
import com.reserveit.dto.ReservationDto;
import com.reserveit.enums.ReservationStatus;
import com.reserveit.logic.interfaces.ReservationService;
import com.reserveit.logic.interfaces.UserService;
import com.reserveit.logic.interfaces.WebSocketService;
import com.reserveit.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class ReservationControllerTest {

    @Mock
    private ReservationService reservationService;
    @Mock
    private WebSocketService webSocketService;
    @Mock
    private UserService userService;

    private ReservationController reservationController;
    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        reservationController = new ReservationController(reservationService, webSocketService, userService);

        // Setup test user
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("test@example.com");
        when(userService.getCurrentUser()).thenReturn(testUser);
    }

    @Test
    void createReservation_Success() {
        // Arrange
        ReservationDto inputDto = new ReservationDto();
        inputDto.setReservationDate(LocalDateTime.now().plusDays(1).toString());
        inputDto.setNumberOfPeople(4);

        ReservationDto savedDto = new ReservationDto();
        savedDto.setId(1L);
        savedDto.setTableId(1L);
        when(reservationService.createReservation(any(ReservationDto.class), any(User.class)))
                .thenReturn(savedDto);

        // Act
        ResponseEntity<?> response = reservationController.createReservation(inputDto);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(webSocketService).notifyTableUpdate(any());
    }

    @Test
    void getReservationsByStatus_Success() {
        // Arrange
        List<ReservationDto> expectedReservations = Arrays.asList(
                new ReservationDto(),
                new ReservationDto()
        );
        when(reservationService.findByStatus(any(ReservationStatus.class)))
                .thenReturn(expectedReservations);

        // Act
        ResponseEntity<List<ReservationDto>> response =
                reservationController.getReservationsByStatus("CONFIRMED");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void cancelReservation_Success() {
        // Arrange
        Long reservationId = 1L;
        doNothing().when(reservationService).cancelReservation(eq(reservationId), any(User.class));

        // Act
        ResponseEntity<?> response = reservationController.cancelReservation(reservationId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(reservationService).cancelReservation(eq(reservationId), any(User.class));
    }

    @Test
    void checkTimeSlotAvailability_True() {
        // Arrange
        UUID companyId = UUID.randomUUID();
        Long tableId = 1L;
        String dateTime = LocalDateTime.now().plusDays(1).toString();

        when(reservationService.isTimeSlotAvailable(companyId, tableId, dateTime, null))
                .thenReturn(true);

        // Act
        ResponseEntity<?> response = reservationController.checkTimeSlotAvailability(
                companyId, tableId, dateTime);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody());
    }

    @Test
    void getMyReservations_Success() {
        // Arrange
        List<ReservationDto> expectedReservations = Arrays.asList(
                new ReservationDto(),
                new ReservationDto()
        );
        when(reservationService.getReservationsByUser(any(User.class)))
                .thenReturn(expectedReservations);

        // Act
        ResponseEntity<List<ReservationDto>> response =
                reservationController.getMyReservations();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
    }
    @Test
    void getReservationById_Success() {
        // Arrange
        Long reservationId = 1L;
        ReservationDto expectedDto = createSampleReservationDto();
        when(reservationService.getReservationById(eq(reservationId), any(User.class)))
                .thenReturn(expectedDto);

        // Act
        ResponseEntity<ReservationDto> response = reservationController.getReservationById(reservationId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedDto, response.getBody());
        verify(reservationService).getReservationById(eq(reservationId), any(User.class));
    }

    @Test
    void getReservationById_NotFound() {
        // Arrange
        Long reservationId = 1L;
        when(reservationService.getReservationById(eq(reservationId), any(User.class)))
                .thenThrow(new IllegalArgumentException("Reservation not found"));

        // Act
        ResponseEntity<ReservationDto> response = reservationController.getReservationById(reservationId);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void updateReservation_Success() {
        // Arrange
        Long reservationId = 1L;
        ReservationDto updateDto = createSampleReservationDto();
        ReservationDto updatedDto = createSampleReservationDto();
        when(reservationService.updateReservation(eq(reservationId), any(ReservationDto.class), any(User.class)))
                .thenReturn(updatedDto);

        // Act
        ResponseEntity<ReservationDto> response = reservationController.updateReservation(reservationId, updateDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(updatedDto, response.getBody());
        verify(reservationService).updateReservation(eq(reservationId), any(ReservationDto.class), any(User.class));
    }

    @Test
    void updateReservation_Error() {
        // Arrange
        Long reservationId = 1L;
        ReservationDto updateDto = createSampleReservationDto();
        when(reservationService.updateReservation(eq(reservationId), any(ReservationDto.class), any(User.class)))
                .thenThrow(new IllegalArgumentException("Invalid update"));

        // Act
        ResponseEntity<ReservationDto> response = reservationController.updateReservation(reservationId, updateDto);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void createQuickReservation_Success() {
        // Arrange
        ReservationDto inputDto = createSampleReservationDto();
        ReservationDto savedDto = createSampleReservationDto();
        savedDto.setId(1L);
        when(reservationService.createQuickReservation(any(ReservationDto.class), anyBoolean(), any(User.class)))
                .thenReturn(savedDto);

        // Act
        ResponseEntity<?> response = reservationController.createQuickReservation(inputDto, false);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        // Body should be a ReservationDto
        ReservationDto body = (ReservationDto) response.getBody();
        assertEquals(savedDto.getId(), body.getId());
        verify(webSocketService).notifyReservationUpdate(savedDto);
        verify(webSocketService).notifyTableUpdate(savedDto.getTableId());
    }

    @Test
    void createQuickReservation_Error() {
        // Arrange
        ReservationDto inputDto = createSampleReservationDto();
        when(reservationService.createQuickReservation(any(ReservationDto.class), anyBoolean(), any(User.class)))
                .thenThrow(new IllegalArgumentException("Invalid reservation"));

        // Act
        ResponseEntity<?> response = reservationController.createQuickReservation(inputDto, false);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> error = (java.util.Map<String, Object>) response.getBody();
        assertEquals("Invalid reservation", error.get("message"));
    }

    @Test
    void deleteAllReservations_Success() {
        // Arrange
        doNothing().when(reservationService).deleteAllReservations();

        // Act
        ResponseEntity<String> response = reservationController.deleteAllReservations();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("All reservations have been deleted successfully", response.getBody());
        verify(reservationService).deleteAllReservations();
    }

    @Test
    void deleteAllReservations_Error() {
        // Arrange
        doThrow(new RuntimeException("Database error"))
                .when(reservationService).deleteAllReservations();

        // Act
        ResponseEntity<String> response = reservationController.deleteAllReservations();

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("Failed to delete reservations"));
    }


    private User createTestUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        return user;
    }

    private ReservationDto createSampleReservationDto() {
        ReservationDto dto = new ReservationDto();
        dto.setId(1L);
        dto.setUserId(UUID.randomUUID());
        dto.setTableId(1L);
        dto.setCompanyId(UUID.randomUUID());
        dto.setReservationDate(LocalDateTime.now().plusHours(1).toString());
        dto.setNumberOfPeople(2);
        dto.setStatus(ReservationStatus.CONFIRMED);
        return dto;
    }
}
