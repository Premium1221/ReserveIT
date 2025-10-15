package controller;

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
}