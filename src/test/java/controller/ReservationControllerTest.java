package controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import com.reserveit.logic.interfaces.ReservationService;
import com.reserveit.dto.ReservationDto;
import com.reserveit.controller.ReservationController;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

class ReservationControllerTest {

    @Mock
    private ReservationService reservationService;

    private ReservationController reservationController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        reservationController = new ReservationController(reservationService);
    }

    @Test
    void createReservation_ValidData_Returns201() {
        // Arrange
        ReservationDto dto = new ReservationDto();
        dto.setCustomerName("John Doe");

        when(reservationService.createReservation(any(ReservationDto.class))).thenReturn(dto);

        // Act
        ResponseEntity<?> response = reservationController.createReservation(dto);

        // Assert
        assertEquals(201, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    void getAllReservations_ReturnsCorrectList() {
        // Arrange
        ReservationDto reservation1 = new ReservationDto();
        ReservationDto reservation2 = new ReservationDto();
        when(reservationService.getAllReservations()).thenReturn(Arrays.asList(reservation1, reservation2));

        // Act
        ResponseEntity<List<ReservationDto>> response = reservationController.getAllReservations();

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void getReservationById_ValidId_ReturnsReservation() {
        // Arrange
        Long reservationId = 1L;
        ReservationDto mockReservation = new ReservationDto();
        when(reservationService.getReservationById(reservationId)).thenReturn(mockReservation);

        // Act
        ResponseEntity<?> response = reservationController.getReservationById(reservationId);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    void getReservationById_InvalidId_Returns400() {
        // Arrange
        Long invalidId = -1L;
        when(reservationService.getReservationById(invalidId))
                .thenThrow(new IllegalArgumentException("Invalid ID"));

        // Act
        ResponseEntity<?> response = reservationController.getReservationById(invalidId);

        // Assert
        assertEquals(400, response.getStatusCodeValue());
    }
}
