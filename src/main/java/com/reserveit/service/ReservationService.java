package com.reserveit.service;

import com.reserveit.dto.ReservationDto;
import java.util.List;

public interface ReservationService {
    ReservationDto createReservation(ReservationDto reservationDto);
    List<ReservationDto> getAllReservations();
    ReservationDto updateReservation(Long id, ReservationDto reservationDto);
}
