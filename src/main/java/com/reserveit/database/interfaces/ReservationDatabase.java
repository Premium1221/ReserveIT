package com.reserveit.database.interfaces;

import com.reserveit.enums.ReservationStatus;
import com.reserveit.model.Company;
import com.reserveit.model.DiningTable;
import com.reserveit.model.Reservation;
import com.reserveit.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationDatabase {
    List<Reservation> findByUser(User user);
    List<Reservation> findByCompany(Company company);
    List<Reservation> findByCompanyAndReservationDateAfterAndStatusNot(Company company, LocalDateTime date, ReservationStatus status);
    List<Reservation> findByCompanyAndReservationDateBetween(Company company, LocalDateTime startDate, LocalDateTime endDate);
    Optional<Reservation> findByCompanyAndTableNumber(Company company, String tableNumber);
    boolean existsOverlappingReservation(Long tableId, LocalDateTime startTime, LocalDateTime endTime);
    void deleteAll();
    Reservation save(Reservation reservation);
    List<Reservation> findAll();
    Optional<Reservation> findById(Long id);
    void deleteById(Long id);
    List<Reservation> findByStatus(ReservationStatus status);
    List<Reservation> findByReservationDateBetween(LocalDateTime startTime, LocalDateTime endTime);
    List<Reservation> findByCompanyAndStatus(Company company, ReservationStatus status);
    List<Reservation> findByDiningTableAndReservationDateBetween(
            DiningTable diningTable,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

}