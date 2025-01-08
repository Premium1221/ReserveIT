package com.reserveit.logic.interfaces;

import com.reserveit.dto.ReservationDto;
import com.reserveit.enums.ReservationStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ReservationService {
    // Existing methods
    ReservationDto createReservation(ReservationDto reservationDto);
    List<ReservationDto> getAllReservations();
    ReservationDto updateReservation(Long id, ReservationDto reservationDto);
    ReservationDto getReservationById(Long id);
    void cancelReservation(Long id);
    List<ReservationDto> getReservationsByCompany(UUID companyId);
    List<ReservationDto> getUpcomingReservations(UUID companyId);
    List<ReservationDto> getReservationsByDate(UUID companyId, LocalDateTime date);
    boolean isTableAvailable(UUID companyId, Long tableId, LocalDateTime dateTime, int duration);

    ReservationDto checkInReservation(Long id);
    ReservationDto checkOutReservation(Long id);
    ReservationDto extendReservation(Long id, Integer duration);
    List<ReservationDto> findByStatus(ReservationStatus status);
    List<ReservationDto> findByCompanyAndStatus(UUID companyId, ReservationStatus status);
    List<ReservationDto> getReservationsByTimeRange(LocalDateTime startTime, LocalDateTime endTime);
    boolean isTimeSlotAvailable(UUID companyId, Long tableId, String dateTimeStr, Integer duration);

    }