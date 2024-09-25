package service;

import com.reserveit.dto.ReservationDto;
import com.reserveit.model.Reservation;
import com.reserveit.repository.HardcodedReservationRepository;
import com.reserveit.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReservationServiceTest {

    private ReservationService reservationService;
    private HardcodedReservationRepository hardcodedReservationRepository;

    @BeforeEach
    void setUp() {
        // Use a hardcoded repository in the test
        hardcodedReservationRepository = new HardcodedReservationRepository();
        reservationService = new ReservationService(hardcodedReservationRepository);
    }

    @Test
    void testCreateReservation() {
        // Arrange
        ReservationDto reservationDto = new ReservationDto();
        reservationDto.setCustomerName("John Doe");
        reservationDto.setReservationDate("2024-09-24");
        reservationDto.setNumberOfPeople(4);

        // Act
        ReservationDto savedReservation = reservationService.createReservation(reservationDto);

        // Assert
        assertNotNull(savedReservation.getId());
        assertEquals("John Doe", savedReservation.getCustomerName());
        assertEquals("2024-09-24", savedReservation.getReservationDate());
        assertEquals(4, savedReservation.getNumberOfPeople());
    }

    @Test
    void testGetAllReservations() {
        // Arrange
        ReservationDto reservation1 = new ReservationDto();
        reservation1.setCustomerName("John Doe");
        reservation1.setReservationDate("2024-09-24");
        reservation1.setNumberOfPeople(4);
        reservationService.createReservation(reservation1);

        ReservationDto reservation2 = new ReservationDto();
        reservation2.setCustomerName("Jane Smith");
        reservation2.setReservationDate("2024-09-25");
        reservation2.setNumberOfPeople(2);
        reservationService.createReservation(reservation2);

        // Act
        List<ReservationDto> reservations = reservationService.getAllReservations();

        // Assert
        assertEquals(2, reservations.size());
        assertEquals("John Doe", reservations.get(0).getCustomerName());
        assertEquals("Jane Smith", reservations.get(1).getCustomerName());
    }

    @Test
    void testUpdateReservation() {
        // Arrange
        ReservationDto reservationDto = new ReservationDto();
        reservationDto.setCustomerName("John Doe");
        reservationDto.setReservationDate("2024-09-24");
        reservationDto.setNumberOfPeople(4);
        ReservationDto savedReservation = reservationService.createReservation(reservationDto);

        savedReservation.setCustomerName("Jane Doe");
        savedReservation.setNumberOfPeople(5);

        // Act
        ReservationDto updatedReservation = reservationService.updateReservation(savedReservation.getId(), savedReservation);

        // Assert
        assertEquals("Jane Doe", updatedReservation.getCustomerName());
        assertEquals(5, updatedReservation.getNumberOfPeople());
    }
}
