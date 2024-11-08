package com.reserveit.logic.impl;

import com.reserveit.database.interfaces.ICompanyDatabase;
import com.reserveit.database.interfaces.IDiningTableDatabase;
import com.reserveit.database.interfaces.IReservationDatabase;
import com.reserveit.dto.ReservationDto;
import com.reserveit.model.Company;
import com.reserveit.model.Reservation;
import com.reserveit.model.DiningTable;
import com.reserveit.logic.interfaces.ReservationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReservationServiceImpl implements ReservationService {
    private final IReservationDatabase reservationDb;
    private final ICompanyDatabase companyDb;
    private final IDiningTableDatabase tableDb;

    public ReservationServiceImpl(IReservationDatabase reservationDb,
                                  ICompanyDatabase companyDb,
                                  IDiningTableDatabase tableDb) {
        this.reservationDb = reservationDb;
        this.companyDb = companyDb;
        this.tableDb = tableDb;
    }
    @Override
    public List<ReservationDto> getReservationsByCompany(UUID companyId) {
        Optional<Company> optionalCompany = Optional.ofNullable(companyDb.findById(companyId));
        Company company = optionalCompany
                .orElseThrow(() -> new IllegalArgumentException("Company not found with id: " + companyId));

        return reservationDb.findByCompany(company)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReservationDto> getUpcomingReservations(UUID companyId) {
        Optional<Company> optionalCompany = Optional.ofNullable(companyDb.findById(companyId));
        Company company = optionalCompany
                .orElseThrow(() -> new IllegalArgumentException("Company not found with id: " + companyId));

        return reservationDb.findByCompanyAndReservationDateAfterAndStatusNot(
                        company,
                        LocalDateTime.now(),
                        Reservation.ReservationStatus.CANCELLED)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReservationDto> getReservationsByDate(UUID companyId, LocalDateTime date) {
        Optional<Company> optionalCompany = Optional.ofNullable(companyDb.findById(companyId));
        Company company = optionalCompany
                .orElseThrow(() -> new IllegalArgumentException("Company not found with id: " + companyId));

        LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        return reservationDb.findByCompanyAndReservationDateBetween(
                        company,
                        startOfDay,
                        endOfDay)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isTableAvailable(UUID companyId, Long tableId, LocalDateTime dateTime, int duration) {
        DiningTable table = tableDb.findById(tableId)
                .orElseThrow(() -> new IllegalArgumentException("Table not found"));

        if (!table.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Table does not belong to the specified company");
        }

        return table.isAvailableForDateTime(dateTime);
    }

    @Override
    public void checkInReservation(Long id) {
        Reservation reservation = reservationDb.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found with id: " + id));

        if (reservation.getStatus() != Reservation.ReservationStatus.CONFIRMED) {
            throw new IllegalStateException("Only confirmed reservations can be checked in");
        }

        if (reservation.getDiningTable() != null) {
            reservation.getDiningTable().setStatus(DiningTable.TableStatus.OCCUPIED);
        }

        reservation.setStatus(Reservation.ReservationStatus.COMPLETED);
        reservationDb.save(reservation);
    }

    @Override
    public void checkOutReservation(Long id) {
        Reservation reservation = reservationDb.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found with id: " + id));

        if (reservation.getStatus() != Reservation.ReservationStatus.COMPLETED) {
            throw new IllegalStateException("Only checked-in reservations can be checked out");
        }

        if (reservation.getDiningTable() != null) {
            reservation.getDiningTable().setStatus(DiningTable.TableStatus.AVAILABLE);
        }

        reservationDb.save(reservation);
    }
    @Override
    public ReservationDto createReservation(ReservationDto reservationDto) {
        // Basic validation
        if (reservationDto.getCustomerName() == null || reservationDto.getCustomerName().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer name cannot be empty");
        }
        if (reservationDto.getNumberOfPeople() <= 0) {
            throw new IllegalArgumentException("Number of people must be greater than 0");
        }

        // Find the company
        Optional<Company> optionalCompany = Optional.ofNullable(companyDb.findById(reservationDto.getCompanyId()));
        Company company = optionalCompany
                .orElseThrow(() -> new IllegalArgumentException("Company not found with id: " + reservationDto.getCompanyId()));

        Reservation reservation = convertToEntity(reservationDto);
        reservation.setCompany(company);
        reservation.setStatus(Reservation.ReservationStatus.PENDING);

        // If a specific table is requested, validate and assign it
        if (reservationDto.getTableNumber() != null) {
            DiningTable table = tableDb.findByCompanyIdAndTableNumber(company.getId(), reservationDto.getTableNumber())
                    .orElseThrow(() -> new IllegalArgumentException("Table not found"));

            if (!table.isAvailableForDateTime(reservation.getReservationDate())) {
                throw new IllegalStateException("Selected table is not available for this time");
            }
            reservation.setDiningTable(table);
        }

        Reservation savedReservation = reservationDb.save(reservation);
        return convertToDto(savedReservation);
    }

    @Override
    public List<ReservationDto> getAllReservations() {
        return reservationDb.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ReservationDto getReservationById(Long id) {
        Optional<Reservation> optionalReservation = reservationDb.findById(id);
        return optionalReservation
                .map(this::convertToDto)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found with id: " + id));
    }

    @Override
    public ReservationDto updateReservation(Long id, ReservationDto reservationDto) {
        Reservation existingReservation = reservationDb.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found with id: " + id));

        // Check if the reservation can be modified
        if (!canBeModified(existingReservation)) {
            throw new IllegalStateException("Reservation cannot be modified");
        }

        // Update basic fields
        existingReservation.setCustomerName(reservationDto.getCustomerName());
        existingReservation.setReservationDate(LocalDateTime.parse(reservationDto.getReservationDate()));
        existingReservation.setNumberOfPeople(reservationDto.getNumberOfPeople());

        // Update company if changed
        if (!existingReservation.getCompany().getId().equals(reservationDto.getCompanyId())) {
            Optional<Company> optionalCompany = Optional.ofNullable(companyDb.findById(reservationDto.getCompanyId()));
            Company newCompany = optionalCompany
                    .orElseThrow(() -> new IllegalArgumentException("Company not found with id: " + reservationDto.getCompanyId()));
            existingReservation.setCompany(newCompany);
        }

        // Update table if specified
        if (reservationDto.getTableNumber() != null) {
            DiningTable newTable = tableDb.findByCompanyIdAndTableNumber(
                            existingReservation.getCompany().getId(),
                            reservationDto.getTableNumber())
                    .orElseThrow(() -> new IllegalArgumentException("Table not found"));

            if (!newTable.isAvailableForDateTime(existingReservation.getReservationDate())) {
                throw new IllegalStateException("Selected table is not available for this time");
            }
            existingReservation.setDiningTable(newTable);
        }

        Reservation updatedReservation = reservationDb.save(existingReservation);
        return convertToDto(updatedReservation);
    }

    @Override
    public void cancelReservation(Long id) {
        Reservation reservation = reservationDb.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found with id: " + id));

        if (reservation.getStatus() == Reservation.ReservationStatus.CANCELLED) {
            throw new IllegalStateException("Reservation is already cancelled");
        }

        if (reservation.getReservationDate().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Cannot cancel past reservations");
        }

        reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
        if (reservation.getDiningTable() != null) {
            reservation.getDiningTable().setStatus(DiningTable.TableStatus.AVAILABLE);
        }
        reservationDb.save(reservation);
    }



    private boolean canBeModified(Reservation reservation) {
        return reservation.getStatus() != Reservation.ReservationStatus.CANCELLED &&
                reservation.getStatus() != Reservation.ReservationStatus.COMPLETED &&
                reservation.getReservationDate().isAfter(LocalDateTime.now());
    }

    private ReservationDto convertToDto(Reservation reservation) {
        ReservationDto dto = new ReservationDto();
        dto.setId(reservation.getId());
        dto.setCustomerName(reservation.getCustomerName());
        dto.setCustomerEmail(reservation.getCustomerEmail());
        dto.setCustomerPhone(reservation.getCustomerPhone());
        dto.setReservationDate(reservation.getReservationDate().toString());
        dto.setNumberOfPeople(reservation.getNumberOfPeople());
        dto.setSpecialRequests(reservation.getSpecialRequests());
        dto.setStatus(reservation.getStatus().toString());

        if (reservation.getCompany() != null) {
            dto.setCompanyId(reservation.getCompany().getId());
            dto.setCompanyName(reservation.getCompany().getName());
        }

        if (reservation.getDiningTable() != null) {
            dto.setTableNumber(reservation.getDiningTable().getTableNumber());
        }

        return dto;
    }

    private Reservation convertToEntity(ReservationDto dto) {
        Reservation reservation = new Reservation();
        reservation.setCustomerName(dto.getCustomerName());
        reservation.setCustomerEmail(dto.getCustomerEmail());
        reservation.setCustomerPhone(dto.getCustomerPhone());
        reservation.setReservationDate(LocalDateTime.parse(dto.getReservationDate()));
        reservation.setNumberOfPeople(dto.getNumberOfPeople());
        reservation.setSpecialRequests(dto.getSpecialRequests());
        return reservation;
    }
}