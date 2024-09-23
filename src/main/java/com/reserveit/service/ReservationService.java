package com.reserveit.service;

import com.reserveit.dto.ReservationDto;
import com.reserveit.model.Reservation;
import com.reserveit.repository.ReservationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;

    // Constructor injection
    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public ReservationDto createReservation(ReservationDto reservationDto) {
        Reservation reservation = convertToEntity(reservationDto);
        Reservation savedReservation = reservationRepository.save(reservation);
        return convertToDto(savedReservation);
    }

    public List<ReservationDto> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public ReservationDto updateReservation(Long id, ReservationDto reservationDto) {
        Reservation reservation = convertToEntity(reservationDto);
        reservation.setId(id);
        Reservation updatedReservation = reservationRepository.save(reservation);
        return convertToDto(updatedReservation);
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
