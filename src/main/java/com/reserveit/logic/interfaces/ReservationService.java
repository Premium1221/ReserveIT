package com.reserveit.logic.interfaces;

import com.reserveit.dto.ReservationDto;
import com.reserveit.enums.ReservationStatus;
import com.reserveit.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ReservationService {
    // Create and modify reservations
    ReservationDto createReservation(ReservationDto reservationDto, User user);
    ReservationDto updateReservation(Long id, ReservationDto reservationDto,User user);
    void cancelReservation(Long id,User user);
    void deleteAllReservations();


        // Fetch reservations
    List<ReservationDto> getAllReservations();
    ReservationDto getReservationById(Long id,User user);
    List<ReservationDto> getReservationsByUser(User user);
    List<ReservationDto> getReservationsByCompany(UUID companyId);
    List<ReservationDto> getUpcomingReservations(UUID companyId);
    List<ReservationDto> getReservationsByDate(UUID companyId, LocalDateTime date);
    List<ReservationDto> getReservationsByTableAndDate(Long tableId, LocalDateTime startDate, LocalDateTime endDate);



    // Staff operations
    ReservationDto checkInReservation(Long id);
    ReservationDto checkOutReservation(Long id);
    ReservationDto extendReservation(Long id, Integer duration);
    ReservationDto markAsNoShow(Long id);

    // Availability checks
    boolean isTableAvailable(UUID companyId, Long tableId, LocalDateTime dateTime, int duration);
    boolean isTimeSlotAvailable(UUID companyId, Long tableId, String dateTimeStr, Integer duration);
    List<Map<String, Object>> checkForLateArrivals();

    // Quick reservations
    ReservationDto createQuickReservation(ReservationDto reservationDto, boolean immediate,User user);

    // Filtering and status
    List<ReservationDto> findByStatus(ReservationStatus status);
    List<ReservationDto> findByCompanyAndStatus(UUID companyId, ReservationStatus status);
    List<ReservationDto> getReservationsByTimeRange(LocalDateTime startTime, LocalDateTime endTime);
    List<ReservationDto> getReservationsForArrivalCheck(UUID restaurantId, LocalDateTime start, LocalDateTime end);
    List<ReservationDto> getExtendedStayReservations(UUID companyId);
}