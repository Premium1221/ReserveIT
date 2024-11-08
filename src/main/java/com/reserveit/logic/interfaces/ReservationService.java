package com.reserveit.logic.interfaces;

import com.reserveit.dto.ReservationDto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ReservationService {
    ReservationDto createReservation(ReservationDto reservationDto);
    List<ReservationDto> getAllReservations();
    ReservationDto updateReservation(Long id, ReservationDto reservationDto);

    // New methods
    ReservationDto getReservationById(Long id);
    void cancelReservation(Long id);
    List<ReservationDto> getReservationsByCompany(UUID companyId);
    List<ReservationDto> getUpcomingReservations(UUID companyId);
    List<ReservationDto> getReservationsByDate(UUID companyId, LocalDateTime date);
    boolean isTableAvailable(UUID companyId, Long tableId, LocalDateTime dateTime, int duration);
    void checkInReservation(Long id);
    void checkOutReservation(Long id);
}