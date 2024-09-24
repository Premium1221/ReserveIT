package com.reserveit.service;

import com.reserveit.dto.ReservationDto;
import com.reserveit.model.Reservation;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReservationService {

    private final List<Reservation> reservations = new ArrayList<>();
    private Long nextId = 1L; // To simulate auto-incrementing ID

    public ReservationDto createReservation(ReservationDto reservationDto) {
        Reservation reservation = convertToEntity(reservationDto);
        reservation.setId(nextId++); // Simulate database ID generation
        reservations.add(reservation);
        return convertToDto(reservation);
    }

    public List<ReservationDto> getAllReservations() {
        return reservations.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public ReservationDto updateReservation(Long id, ReservationDto reservationDto) {
        Optional<Reservation> existingReservation = reservations.stream()
                .filter(r -> r.getId().equals(id))
                .findFirst();

        if (existingReservation.isPresent()) {
            Reservation updatedReservation = convertToEntity(reservationDto);
            updatedReservation.setId(id);
            reservations.remove(existingReservation.get());
            reservations.add(updatedReservation);
            return convertToDto(updatedReservation);
        }

        throw new IllegalArgumentException("Reservation not found with id: " + id);
    }

    // Conversion methods
    private ReservationDto convertToDto(Reservation reservation) {
        ReservationDto dto = new ReservationDto();
        dto.setId(reservation.getId());
        dto.setCustomerName(reservation.getCustomerName());
        dto.setReservationDate(reservation.getReservationDate().toString());
        dto.setNumberOfPeople(reservation.getNumberOfPeople());
        return dto;
    }

    private Reservation convertToEntity(ReservationDto dto) {
        Reservation reservation = new Reservation();
        reservation.setCustomerName(dto.getCustomerName());
        reservation.setReservationDate(LocalDate.parse(dto.getReservationDate()));
        reservation.setNumberOfPeople(dto.getNumberOfPeople());
        return reservation;
    }
}
