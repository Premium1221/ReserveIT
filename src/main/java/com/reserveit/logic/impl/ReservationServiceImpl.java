package com.reserveit.logic.impl;

import com.reserveit.database.interfaces.CompanyDatabase;
import com.reserveit.database.interfaces.DiningTableDatabase;
import com.reserveit.database.interfaces.ReservationDatabase;
import com.reserveit.dto.ReservationDto;
import com.reserveit.model.Company;
import com.reserveit.model.Reservation;
import com.reserveit.model.DiningTable;
import com.reserveit.logic.interfaces.ReservationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.reserveit.enums.ReservationStatus;
import com.reserveit.enums.TableStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReservationServiceImpl implements ReservationService {
    private final ReservationDatabase reservationDb;
    private final CompanyDatabase companyDb;
    private final DiningTableDatabase tableDb;

    public ReservationServiceImpl(ReservationDatabase reservationDb,
                                  CompanyDatabase companyDb,
                                  DiningTableDatabase tableDb) {
        this.reservationDb = reservationDb;
        this.companyDb = companyDb;
        this.tableDb = tableDb;
    }

    @Override
    public List<ReservationDto> getReservationsByCompany(UUID companyId) {
        Company company = companyDb.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found with id: " + companyId));

        return reservationDb.findByCompany(company)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }


    @Override
    public List<ReservationDto> getUpcomingReservations(UUID companyId) {
        Company company = companyDb.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found with id: " + companyId));

        return reservationDb.findByCompanyAndReservationDateAfterAndStatusNot(
                        company,
                        LocalDateTime.now(),
                        ReservationStatus.CANCELLED)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReservationDto> getReservationsByDate(UUID companyId, LocalDateTime date) {
        Company company = companyDb.findById(companyId)
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
    public ReservationDto checkInReservation(Long id) {
        Reservation reservation = reservationDb.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found with id: " + id));

        if (!canCheckIn(reservation)) {
            throw new IllegalStateException("Cannot check in at this time");
        }

        reservation.setStatus(ReservationStatus.ARRIVED);
        reservation.setCheckInTime(LocalDateTime.now());

        if (reservation.getDiningTable() != null) {
            reservation.getDiningTable().setStatus(TableStatus.OCCUPIED);
        }

        Reservation savedReservation = reservationDb.save(reservation);
        return convertToDto(savedReservation);
    }

    @Override
    public ReservationDto checkOutReservation(Long id) {
        Reservation reservation = reservationDb.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found with id: " + id));

        if (reservation.getStatus() != ReservationStatus.ARRIVED) {
            throw new IllegalStateException("Only arrived reservations can be checked out");
        }

        reservation.setStatus(ReservationStatus.COMPLETED);
        reservation.setCheckOutTime(LocalDateTime.now());

        if (reservation.getDiningTable() != null) {
            reservation.getDiningTable().setStatus(TableStatus.AVAILABLE);
        }

        Reservation savedReservation = reservationDb.save(reservation);
        return convertToDto(savedReservation);
    }



    @Override
    public ReservationDto createReservation(ReservationDto reservationDto) {
        if (reservationDto.getCustomerName() == null || reservationDto.getCustomerName().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer name cannot be empty");
        }
        if (reservationDto.getNumberOfPeople() <= 0) {
            throw new IllegalArgumentException("Number of people must be greater than 0");
        }

        Company company = companyDb.findById(reservationDto.getCompanyId())
                .orElseThrow(() -> new IllegalArgumentException("Company not found with id: " + reservationDto.getCompanyId()));

        Reservation reservation = convertToEntity(reservationDto);
        reservation.setCompany(company);
        reservation.setStatus(ReservationStatus.CONFIRMED);

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

        if (!canBeModified(existingReservation)) {
            throw new IllegalStateException("Reservation cannot be modified");
        }

        existingReservation.setCustomerName(reservationDto.getCustomerName());
        existingReservation.setReservationDate(LocalDateTime.parse(reservationDto.getReservationDate()));
        existingReservation.setNumberOfPeople(reservationDto.getNumberOfPeople());

        if (!existingReservation.getCompany().getId().equals(reservationDto.getCompanyId())) {
            Company newCompany = companyDb.findById(reservationDto.getCompanyId())
                    .orElseThrow(() -> new IllegalArgumentException("Company not found with id: " + reservationDto.getCompanyId()));
            existingReservation.setCompany(newCompany);
        }

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


    public void cancelReservation(Long id) {
        Reservation reservation = reservationDb.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found with id: " + id));

        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new IllegalStateException("Only confirmed reservations can be cancelled");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        if (reservation.getDiningTable() != null) {
            reservation.getDiningTable().setStatus(TableStatus.AVAILABLE);
        }

        reservationDb.save(reservation);
    }
    @Override
    public List<ReservationDto> findByStatus(ReservationStatus status) {
        return reservationDb.findByStatus(status)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    @Override
    public ReservationDto extendReservation(Long id, Integer duration) {
        Reservation reservation = reservationDb.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found with id: " + id));

        if (reservation.getStatus() != ReservationStatus.ARRIVED) {
            throw new IllegalStateException("Can only extend active reservations");
        }

        // Add the additional duration and update end time
        reservation.setDuration(reservation.getDuration() + duration);
        reservation.setEndTime(reservation.getEndTime().plusMinutes(duration));

        Reservation savedReservation = reservationDb.save(reservation);
        return convertToDto(savedReservation);
    }
    @Override
    public List<ReservationDto> findByCompanyAndStatus(UUID companyId, ReservationStatus status) {
        Company company = companyDb.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found with id: " + companyId));

        return reservationDb.findByCompanyAndStatus(company, status)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    @Override
    public boolean isTimeSlotAvailable(UUID companyId, Long tableId, String dateTimeStr, Integer duration) {
        LocalDateTime requestedDateTime = LocalDateTime.parse(dateTimeStr);

        LocalDateTime requestedEndTime = requestedDateTime.plusMinutes(duration);

        DiningTable table = tableDb.findById(tableId)
                .orElseThrow(() -> new IllegalArgumentException("Table not found"));

        if (!table.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Table does not belong to specified company");
        }

        boolean hasOverlap = reservationDb.existsOverlappingReservation(
                tableId,
                requestedDateTime,
                requestedEndTime
        );

        return !hasOverlap && table.getStatus() != TableStatus.OUT_OF_SERVICE;
    }

    private boolean canCheckIn(Reservation reservation) {
        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reservationTime = reservation.getReservationDate();

        long minutesDifference = Math.abs(java.time.Duration.between(now, reservationTime).toMinutes());

        // Allow check-in within 15 minutes before or after reservation time
        return minutesDifference <= 15;
    }

    @Override
    public List<ReservationDto> getReservationsByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return reservationDb.findByReservationDateBetween(startTime, endTime)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }


    private boolean canBeModified(Reservation reservation) {
        return reservation.getStatus() != ReservationStatus.CANCELLED &&
                reservation.getStatus() != ReservationStatus.COMPLETED &&
                reservation.getReservationDate().isAfter(LocalDateTime.now());
    }

    private ReservationDto convertToDto(Reservation reservation) {
        ReservationDto dto = new ReservationDto();
        // Existing fields
        dto.setId(reservation.getId());
        dto.setCustomerName(reservation.getCustomerName());
        dto.setCustomerEmail(reservation.getCustomerEmail());
        dto.setCustomerPhone(reservation.getCustomerPhone());
        dto.setReservationDate(reservation.getReservationDate().toString());
        dto.setNumberOfPeople(reservation.getNumberOfPeople());
        dto.setSpecialRequests(reservation.getSpecialRequests());
        dto.setStatus(reservation.getStatus());

        // New fields
        dto.setDuration(reservation.getDuration());
        if (reservation.getEndTime() != null) {
            dto.setEndTime(reservation.getEndTime());
        }
        if (reservation.getCheckInTime() != null) {
            dto.setCheckedInAt(reservation.getCheckInTime());
        }
        if (reservation.getCheckOutTime() != null) {
            dto.setCheckedOutAt(reservation.getCheckOutTime());
        }

        if (reservation.getCompany() != null) {
            dto.setCompanyId(reservation.getCompany().getId());
            dto.setCompanyName(reservation.getCompany().getName());
        }

        if (reservation.getDiningTable() != null) {
            dto.setTableId(reservation.getDiningTable().getId());
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

        reservation.setDuration(dto.getDuration() != null ? dto.getDuration() : 120);
        LocalDateTime startTime = LocalDateTime.parse(dto.getReservationDate());
        reservation.setEndTime(startTime.plusMinutes(reservation.getDuration()));

        return reservation;
    }
}