package com.reserveit.service.impl;

import com.reserveit.dto.ReservationDto;
import com.reserveit.model.Company;
import com.reserveit.model.Reservation;
import com.reserveit.model.DiningTable;
import com.reserveit.repository.CompanyRepository;
import com.reserveit.repository.ReservationRepository;
import com.reserveit.repository.DiningTableRepository;
import com.reserveit.service.ReservationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReservationServiceImpl implements ReservationService {
    private final ReservationRepository reservationRepository;
    private final CompanyRepository companyRepository;
    private final DiningTableRepository tableRepository;

    public ReservationServiceImpl(ReservationRepository reservationRepository,
                                  CompanyRepository companyRepository,
                                  DiningTableRepository tableRepository) {
        this.reservationRepository = reservationRepository;
        this.companyRepository = companyRepository;
        this.tableRepository = tableRepository;
    }
    @Override
    public List<ReservationDto> getReservationsByCompany(UUID companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found with id: " + companyId));

        return reservationRepository.findByCompany(company)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReservationDto> getUpcomingReservations(UUID companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found with id: " + companyId));

        return reservationRepository.findByCompanyAndReservationDateAfterAndStatusNot(
                        company,
                        LocalDateTime.now(),
                        Reservation.ReservationStatus.CANCELLED)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReservationDto> getReservationsByDate(UUID companyId, LocalDateTime date) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found with id: " + companyId));

        LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        return reservationRepository.findByCompanyAndReservationDateBetween(
                        company,
                        startOfDay,
                        endOfDay)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isTableAvailable(UUID companyId, Long tableId, LocalDateTime dateTime, int duration) {
        DiningTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new IllegalArgumentException("Table not found"));

        if (!table.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Table does not belong to the specified company");
        }

        return table.isAvailableForDateTime(dateTime);
    }

    @Override
    public void checkInReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found with id: " + id));

        if (reservation.getStatus() != Reservation.ReservationStatus.CONFIRMED) {
            throw new IllegalStateException("Only confirmed reservations can be checked in");
        }

        if (reservation.getDiningTable() != null) {
            reservation.getDiningTable().setStatus(DiningTable.TableStatus.OCCUPIED);
        }

        reservation.setStatus(Reservation.ReservationStatus.COMPLETED);
        reservationRepository.save(reservation);
    }

    @Override
    public void checkOutReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found with id: " + id));

        if (reservation.getStatus() != Reservation.ReservationStatus.COMPLETED) {
            throw new IllegalStateException("Only checked-in reservations can be checked out");
        }

        if (reservation.getDiningTable() != null) {
            reservation.getDiningTable().setStatus(DiningTable.TableStatus.AVAILABLE);
        }

        reservationRepository.save(reservation);
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
        Company company = companyRepository.findById(reservationDto.getCompanyId())
                .orElseThrow(() -> new IllegalArgumentException("Company not found with id: " + reservationDto.getCompanyId()));

        Reservation reservation = convertToEntity(reservationDto);
        reservation.setCompany(company);
        reservation.setStatus(Reservation.ReservationStatus.PENDING);

        // If a specific table is requested, validate and assign it
        if (reservationDto.getTableNumber() != null) {
            DiningTable table = tableRepository.findByCompanyIdAndTableNumber(company.getId(), reservationDto.getTableNumber())
                    .orElseThrow(() -> new IllegalArgumentException("Table not found"));

            if (!table.isAvailableForDateTime(reservation.getReservationDate())) {
                throw new IllegalStateException("Selected table is not available for this time");
            }
            reservation.setDiningTable(table);
        }

        Reservation savedReservation = reservationRepository.save(reservation);
        return convertToDto(savedReservation);
    }

    @Override
    public List<ReservationDto> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ReservationDto getReservationById(Long id) {
        return reservationRepository.findById(id)
                .map(this::convertToDto)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found with id: " + id));
    }

    @Override
    public ReservationDto updateReservation(Long id, ReservationDto reservationDto) {
        Reservation existingReservation = reservationRepository.findById(id)
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
            Company newCompany = companyRepository.findById(reservationDto.getCompanyId())
                    .orElseThrow(() -> new IllegalArgumentException("Company not found with id: " + reservationDto.getCompanyId()));
            existingReservation.setCompany(newCompany);
        }

        // Update table if specified
        if (reservationDto.getTableNumber() != null) {
            DiningTable newTable = tableRepository.findByCompanyIdAndTableNumber(
                            existingReservation.getCompany().getId(),
                            reservationDto.getTableNumber())
                    .orElseThrow(() -> new IllegalArgumentException("Table not found"));

            if (!newTable.isAvailableForDateTime(existingReservation.getReservationDate())) {
                throw new IllegalStateException("Selected table is not available for this time");
            }
            existingReservation.setDiningTable(newTable);
        }

        Reservation updatedReservation = reservationRepository.save(existingReservation);
        return convertToDto(updatedReservation);
    }

    @Override
    public void cancelReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
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
        reservationRepository.save(reservation);
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
        dto.setReservationDate(reservation.getReservationDate().toString());
        dto.setNumberOfPeople(reservation.getNumberOfPeople());

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
        reservation.setReservationDate(LocalDateTime.parse(dto.getReservationDate()));
        reservation.setNumberOfPeople(dto.getNumberOfPeople());
        return reservation;
    }
}