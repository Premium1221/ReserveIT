package com.reserveit.database.interfaces;

import com.reserveit.model.Company;
import com.reserveit.model.Reservation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationDatabase {
    List<Reservation> findByCompany(Company company);
    List<Reservation> findByCompanyAndReservationDateAfterAndStatusNot(Company company, LocalDateTime date, Reservation.ReservationStatus status);
    List<Reservation> findByCompanyAndReservationDateBetween(Company company, LocalDateTime startDate, LocalDateTime endDate);
    Optional<Reservation> findByCompanyAndTableNumber(Company company, String tableNumber);
    boolean existsOverlappingReservation(Long tableId, LocalDateTime startTime, LocalDateTime endTime);
    Reservation save(Reservation reservation);
    List<Reservation> findAll();
    Optional<Reservation> findById(Long id);
    void deleteById(Long id);
}