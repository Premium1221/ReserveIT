package service;

import com.reserveit.dto.ReservationDto;
import com.reserveit.repository.HardcodedReservationRepository;
import com.reserveit.service.impl.ReservationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReservationServiceTest {

    private ReservationServiceImpl reservationServiceImpl;
    private HardcodedReservationRepository hardcodedReservationRepository;

    @BeforeEach
    void setUp() {
        hardcodedReservationRepository = new HardcodedReservationRepository();
        reservationServiceImpl = new ReservationServiceImpl(hardcodedReservationRepository);
    }

    @Test
    void testCreateReservation() {
        // Arrange
        ReservationDto reservationDto = new ReservationDto();
        reservationDto.setCustomerName("John Doe");
        reservationDto.setReservationDate("2024-09-24");
        reservationDto.setNumberOfPeople(4);

        // Act
        ReservationDto savedReservation = reservationServiceImpl.createReservation(reservationDto);

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
        reservationServiceImpl.createReservation(reservation1);

        ReservationDto reservation2 = new ReservationDto();
        reservation2.setCustomerName("Jane Smith");
        reservation2.setReservationDate("2024-09-25");
        reservation2.setNumberOfPeople(2);
        reservationServiceImpl.createReservation(reservation2);

        // Act
        List<ReservationDto> reservations = reservationServiceImpl.getAllReservations();

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
        ReservationDto savedReservation = reservationServiceImpl.createReservation(reservationDto);

        savedReservation.setCustomerName("Jane Doe");
        savedReservation.setNumberOfPeople(5);

        // Act
        ReservationDto updatedReservation = reservationServiceImpl.updateReservation(savedReservation.getId(), savedReservation);

        // Assert
        assertEquals("Jane Doe", updatedReservation.getCustomerName());
        assertEquals(5, updatedReservation.getNumberOfPeople());
    }
}
