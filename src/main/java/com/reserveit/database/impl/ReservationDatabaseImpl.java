package com.reserveit.database.impl;

import com.reserveit.database.interfaces.ReservationDatabase;
import com.reserveit.model.Company;
import com.reserveit.model.Reservation;
import com.reserveit.repository.ReservationRepository;
import org.springframework.stereotype.Component;
import com.reserveit.enums.ReservationStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class ReservationDatabaseImpl implements ReservationDatabase {
    private final ReservationRepository reservationRepository;

    public ReservationDatabaseImpl(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Override
    public List<Reservation> findByCompany(Company company) {
        return reservationRepository.findByCompany(company);
    }

    @Override
    public List<Reservation> findByCompanyAndReservationDateAfterAndStatusNot(Company company, LocalDateTime date, ReservationStatus status) {
        return reservationRepository.findByCompanyAndReservationDateAfterAndStatusNot(company, date, status);
    }

    @Override
    public List<Reservation> findByCompanyAndReservationDateBetween(Company company, LocalDateTime startDate, LocalDateTime endDate) {
        return reservationRepository.findByCompanyAndReservationDateBetween(company, startDate, endDate);
    }

    @Override
    public Optional<Reservation> findByCompanyAndTableNumber(Company company, String tableNumber) {
        return reservationRepository.findByCompanyAndTableNumber(company, tableNumber);
    }

    @Override
    public boolean existsOverlappingReservation(Long tableId, LocalDateTime startTime, LocalDateTime endTime) {
        return reservationRepository.existsOverlappingReservation(tableId, startTime, endTime);
    }

    @Override
    public Reservation save(Reservation reservation) {
        return reservationRepository.save(reservation);
    }

    @Override
    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        return reservationRepository.findById(id);
    }

    @Override
    public void deleteById(Long id) {
        reservationRepository.deleteById(id);
    }

    @Override
    public List<Reservation> findByStatus(ReservationStatus status) {
        return reservationRepository.findByStatus(status);
    }
    @Override
    public List<Reservation> findByReservationDateBetween(LocalDateTime startTime, LocalDateTime endTime) {
        return reservationRepository.findByReservationDateBetween(startTime, endTime);
    }
    @Override
    public List<Reservation> findByCompanyAndStatus(Company company, ReservationStatus status) {
        return reservationRepository.findByCompanyAndStatus(company, status);
    }

}
