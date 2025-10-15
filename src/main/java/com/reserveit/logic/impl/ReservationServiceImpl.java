package com.reserveit.logic.impl;

import com.reserveit.database.interfaces.CompanyDatabase;
import com.reserveit.database.interfaces.DiningTableDatabase;
import com.reserveit.database.interfaces.ReservationDatabase;
import com.reserveit.dto.ReservationDto;
import com.reserveit.logic.interfaces.TableAllocationService;
import com.reserveit.model.Company;
import com.reserveit.model.Reservation;
import com.reserveit.model.DiningTable;
import com.reserveit.logic.interfaces.ReservationService;
import com.reserveit.model.User;
import com.reserveit.util.ReservationUtil;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.reserveit.enums.ReservationStatus;
import com.reserveit.enums.TableStatus;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class ReservationServiceImpl implements ReservationService {
    private final ReservationDatabase reservationDb;
    private final CompanyDatabase companyDb;
    private final DiningTableDatabase tableDb;
    private final ReservationUtil reservationUtil;
    private final TableAllocationService tableAllocationService;

    private static final String RESERVATION_NOT_FOUND = "Reservation not found";
    private static final String COMPANY_NOT_FOUND = "Company not found";
    private static final String TABLE_NOT_FOUND = "Table not found";
    private static final String NUMBER_PEOPLE_GREATER = "Number of people must be greater than 0";
    private static final String COMPANY_ID_NOT_FOUND = "Company ID not found";


    public ReservationServiceImpl(
            ReservationDatabase reservationDb,
            CompanyDatabase companyDb,
            DiningTableDatabase tableDb,
            ReservationUtil reservationUtil,
            TableAllocationServiceImpl tableAllocationService)
    {
        this.reservationDb = reservationDb;
        this.companyDb = companyDb;
        this.tableDb = tableDb;
        this.reservationUtil = reservationUtil;
        this.tableAllocationService = tableAllocationService;
    }




    @Override
    public List<ReservationDto> getUpcomingReservations(UUID companyId) {
        Company company = companyDb.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException(COMPANY_ID_NOT_FOUND + companyId));

        return reservationDb.findByCompanyAndReservationDateAfterAndStatusNot(
                        company,
                        LocalDateTime.now(),
                        ReservationStatus.CANCELLED)
                .stream()
                .map(this::convertToDto)
                .toList();
    }
    @Override
    public void deleteAllReservations() {
        reservationDb.deleteAll();
    }

    @Override
    public List<ReservationDto> getReservationsByDate(UUID companyId, LocalDateTime date) {
        Company company = companyDb.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException(COMPANY_ID_NOT_FOUND + companyId));

        LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        return reservationDb.findByCompanyAndReservationDateBetween(
                        company,
                        startOfDay,
                        endOfDay)
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public boolean isTableAvailable(UUID companyId, Long tableId, LocalDateTime dateTime, int duration) {
        DiningTable table = tableDb.findById(tableId)
                .orElseThrow(() -> new IllegalArgumentException(TABLE_NOT_FOUND));

        if (!table.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Table does not belong to the specified company");
        }

        return table.isAvailableForDateTime(dateTime);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized ReservationDto checkInReservation(Long id) {
        try {
            Reservation reservation = reservationDb.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException(RESERVATION_NOT_FOUND));

            if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
                throw new IllegalStateException("Can only check in confirmed reservations");
            }

            DiningTable table = reservation.getDiningTable();
            if (table == null) {
                throw new IllegalStateException("Cannot check in reservation without an assigned table");
            }

            boolean hasActiveReservation = table.getReservations().stream()
                    .anyMatch(r -> r.getStatus() == ReservationStatus.ARRIVED);
            if (hasActiveReservation) {
                throw new IllegalStateException(
                        "Cannot check in: Table has an active reservation. Please wait for check-out."
                );
            }

            reservation.setStatus(ReservationStatus.ARRIVED);
            reservation.setCheckInTime(LocalDateTime.now());

            table.setStatus(TableStatus.OCCUPIED);
            tableDb.save(table);

            Reservation savedReservation = reservationDb.save(reservation);

            return convertToDto(savedReservation);
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to check in reservation: " + e.getMessage()); // Changed from RuntimeException
        }
    }

    @Override
    @Transactional
    public ReservationDto checkOutReservation(Long id) {
        try {
            Reservation reservation = reservationDb.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException(RESERVATION_NOT_FOUND));

            if (reservation.getStatus() != ReservationStatus.ARRIVED) {
                throw new IllegalStateException("Only arrived reservations can be checked out"); // Changed from RuntimeException
            }

            DiningTable table = reservation.getDiningTable();
            if (table != null) {
                table.setStatus(TableStatus.AVAILABLE);
                tableDb.save(table);
            }

            reservation.setStatus(ReservationStatus.COMPLETED);
            reservation.setEndTime(LocalDateTime.now());
            reservation.setCheckOutTime(LocalDateTime.now());

            Reservation savedReservation = reservationDb.save(reservation);
            return convertToDto(savedReservation);
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw e; // Re-throw the specific exceptions
        } catch (Exception e) {
            throw new IllegalStateException("Failed to check out reservation: " + e.getMessage()); // Changed from RuntimeException
        }
    }


    @Override
    public ReservationDto createReservation(ReservationDto dto, User user) {
        validateBasicReservationData(dto);
        Company company = findAndValidateCompany(dto.getCompanyId());
            LocalDateTime reservationTime = parseAndValidateReservationTime(dto.getReservationDate());
        int duration = ReservationUtil.calculateDefaultDuration(reservationTime);
        dto.setDurationMinutes(duration);
        DiningTable table = findAppropriateTable(company, dto, reservationTime);

        Reservation reservation = convertToEntity(dto, user);
        reservation.setCompany(company);
        reservation.setDiningTable(table);


        reservation.setEndTime(reservationTime.plusMinutes(duration));



        Reservation savedReservation = reservationDb.save(reservation);

        return convertToDto(savedReservation);
    }

    private DiningTable findAppropriateTable(Company company, ReservationDto dto, LocalDateTime reservationTime) {
        if (dto.getTableId() != null) {
            DiningTable requestedTable = tableDb.findById(dto.getTableId())
                    .orElseThrow(() -> new IllegalArgumentException("Requested table not found"));

            if (!isTableAvailable(company.getId(), requestedTable.getId(), reservationTime, dto.getDurationMinutes())) {
                throw new IllegalStateException("Requested table is not available for the selected time");
            }
            return requestedTable;
        }

        // Find the list of available tables for the company
        List<DiningTable> availableTables = tableDb.findByCompanyId(company.getId());
        if (availableTables.isEmpty()) {
            throw new IllegalStateException("No tables available for the company");
        }

        // Filter tables based on availability and suitability
        List<DiningTable> suitableTables = availableTables.stream()
                .filter(table -> isTableAvailable(company.getId(), table.getId(), reservationTime, dto.getDurationMinutes()))
                .toList();

        if (suitableTables.isEmpty()) {
            throw new IllegalStateException("No suitable tables available for the requested time");
        }

        // Use TableAllocationService to find the optimal table
        Optional<DiningTable> optimalTable = tableAllocationService.findOptimalTable(
                suitableTables, dto.getNumberOfPeople()
        );

        return optimalTable.orElseThrow(() ->
                new IllegalStateException("No optimal tables found for the given criteria"));
    }


    @Override
    public List<ReservationDto> getAllReservations() {
        return reservationDb.findAll().stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public ReservationDto getReservationById(Long id, User user) {
        Reservation reservation = reservationDb.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(RESERVATION_NOT_FOUND));

        validateReservationOwnership(reservation, user);
        return convertToDto(reservation);
    }


    @Override
    public ReservationDto updateReservation(Long id, ReservationDto reservationDto, User user) {
        Reservation existingReservation = reservationDb.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(RESERVATION_NOT_FOUND));

        validateReservationOwnership(existingReservation, user);

        if (!canModifyReservation(existingReservation)) {
            throw new IllegalStateException("Reservation cannot be modified");
        }

        // Update reservation fields
        if (reservationDto.getReservationDate() != null) {
            LocalDateTime newDate = LocalDateTime.parse(reservationDto.getReservationDate());
            if (!reservationUtil.isValidReservationTime(newDate)) {
                throw new IllegalArgumentException("Invalid reservation time");
            }
            existingReservation.setReservationDate(newDate);
        }

        if (reservationDto.getNumberOfPeople() > 0) {
            existingReservation.setNumberOfPeople(reservationDto.getNumberOfPeople());
        }

        if (reservationDto.getSpecialRequests() != null) {
            existingReservation.setSpecialRequests(reservationDto.getSpecialRequests());
        }

        if (reservationDto.getTableId() != null &&
                !reservationDto.getTableId().equals(existingReservation.getDiningTable().getId())) {
            DiningTable newTable = findAndValidateTable(reservationDto.getTableId(),
                    existingReservation.getCompany().getId());
            existingReservation.setDiningTable(newTable);
        }

        existingReservation.setEndTime(existingReservation.getReservationDate()
                .plusMinutes(reservationDto.getDurationMinutes()));

        Reservation updatedReservation = reservationDb.save(existingReservation);
        return convertToDto(updatedReservation);
    }

    @Override
    public void cancelReservation(Long id, User user) {
        Reservation reservation = reservationDb.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(RESERVATION_NOT_FOUND));

        validateReservationOwnership(reservation, user);

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
                .toList();
    }

    @Override
    public boolean isTimeSlotAvailable(UUID companyId, Long tableId, String dateTimeStr, Integer duration) {
        try {
            DiningTable table = tableDb.findById(tableId)
                    .orElseThrow(() -> new IllegalArgumentException(TABLE_NOT_FOUND));

            LocalDateTime requestedDateTime = LocalDateTime.parse(dateTimeStr);
            if(!reservationUtil.isValidReservationTime(requestedDateTime)){
                return false;
            }

            if (table.getStatus() == TableStatus.OUT_OF_SERVICE) {
                return false;
            }

            if(duration == null || duration < 0){
                duration = ReservationUtil.calculateDefaultDuration(requestedDateTime);
            }

            LocalDateTime requestedEndTime = requestedDateTime.plusMinutes(duration);

            // Debug logs
            log.info("Checking availability for:");
            log.info("Table ID: {}", tableId);
            log.info("Requested start: {}", requestedDateTime);
            log.info("Duration: {} minutes", duration);
            log.info("Requested end: {}", requestedEndTime);
            log.info("Table status: {}", table.getStatus());

            return !reservationDb.existsOverlappingReservation(
                    tableId,
                    requestedDateTime,
                    requestedEndTime
            );
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error checking time slot availability: {}", e.getMessage());
            return false;
        }
    }



    @Override
    public List<ReservationDto> getReservationsByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return reservationDb.findByReservationDateBetween(startTime, endTime)
                .stream()
                .map(this::convertToDto)
                .toList();
    }
    @Override
    public List<ReservationDto> getReservationsForArrivalCheck(UUID restaurantId, LocalDateTime start, LocalDateTime end) {
        Company company = companyDb.findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException(COMPANY_NOT_FOUND));

        return reservationDb.findByCompanyAndStatus(company, ReservationStatus.CONFIRMED)
                .stream()
                .filter(r -> r.getReservationDate().isAfter(start) && r.getReservationDate().isBefore(end))
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public ReservationDto createQuickReservation(ReservationDto dto, boolean immediate, User user) {
        validateQuickReservation(dto);
        Company company = findAndValidateCompany(dto.getCompanyId());
        DiningTable table = findAndValidateTable(dto.getTableId(), company.getId());
        LocalDateTime reservationTime = calculateReservationTime(immediate);

        // Validate time slot availability
        if (!isTimeSlotAvailable(company.getId(), table.getId(), reservationTime.toString(),
                ReservationUtil.calculateDefaultDuration(reservationTime))) {
            throw new IllegalStateException("Selected time slot is not available");
        }

        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setCompany(company);
        reservation.setDiningTable(table);
        reservation.setReservationDate(reservationTime);
        reservation.setNumberOfPeople(dto.getNumberOfPeople());
        reservation.setSpecialRequests(dto.getSpecialRequests());
        reservation.setStatus(ReservationStatus.CONFIRMED);

        // Set appropriate duration based on time of day
        int duration = ReservationUtil.calculateDefaultDuration(reservationTime);
        reservation.setDuration(duration);
        reservation.setEndTime(reservationTime.plusMinutes(duration));

        // Update table status based on whether it's immediate or not
        table.setStatus(immediate ? TableStatus.OCCUPIED : TableStatus.RESERVED);
        tableDb.save(table);

        Reservation savedReservation = reservationDb.save(reservation);
        return convertToDto(savedReservation);
    }


    public Company findAndValidateCompany(UUID companyId) {
        return companyDb.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found with id: " + companyId));
    }

    private void validateQuickReservation(ReservationDto dto) {
        if (dto.getNumberOfPeople() <= 0) {
            throw new IllegalArgumentException(NUMBER_PEOPLE_GREATER);
        }
        if (dto.getCompanyId() == null) {
            throw new IllegalArgumentException("Company ID is required");
        }
        if (dto.getTableId() == null) {
            throw new IllegalArgumentException("Table ID is required");
        }
    }


    private DiningTable findAndValidateTable(Long tableId, UUID companyId) {
        DiningTable table = tableDb.findById(tableId)
                .orElseThrow(() -> new IllegalArgumentException(TABLE_NOT_FOUND));

        if (table.getCompany() == null || !table.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("Table does not belong to the specified company");
        }

        return table;
    }



    private LocalDateTime calculateReservationTime(boolean immediate) {
        if (immediate) {
            return LocalDateTime.now().plusMinutes(2);
        }
        return LocalDateTime.now().plusHours(1);
    }


    public List<Map<String, Object>> checkForLateArrivals() {
        List<Map<String, Object>> notifications = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime checkWindow = now.minusMinutes(15); // Adjusted to 15 minutes

        try {
            List<Reservation> confirmedReservations = reservationDb.findByStatus(ReservationStatus.CONFIRMED);

            for (Reservation reservation : confirmedReservations) {
                LocalDateTime reservationTime = reservation.getReservationDate();

                if (reservationTime.isBefore(checkWindow)) {
                    Map<String, Object> notification = new HashMap<>();
                    notification.put("type", "LATE_ARRIVAL");
                    notification.put("reservationId", reservation.getId());
                    notification.put("message", "Reservation is over 15 minutes late");
                    notification.put("userName", reservation.getUser().getFullName());
                    notification.put("tableNumber", reservation.getDiningTable() != null ? reservation.getDiningTable().getTableNumber() : null);
                    notification.put("companyId", reservation.getCompany().getId());

                    notifications.add(notification);
                }
            }
        } catch (Exception e) {
            log.error("Error checking late arrivals: {}", e.getMessage());
        }

        return notifications;
    }

    @Override
    public List<ReservationDto> getExtendedStayReservations(UUID companyId) {
        Company company = companyDb.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException(COMPANY_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now();

        // Get all ARRIVED reservations for this company
        return reservationDb.findByCompanyAndStatus(company, ReservationStatus.ARRIVED)
                .stream()
                .filter(reservation -> {
                    // Calculate when the reservation should end
                    LocalDateTime expectedEndTime = reservation.getReservationDate()
                            .plusMinutes(reservation.getDuration());

                    // Check if current time is past the expected end time
                    return now.isAfter(expectedEndTime);
                })
                .map(this::convertToDto)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReservationDto markAsNoShow(Long id) {
        try {
            Reservation reservation = reservationDb.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException(RESERVATION_NOT_FOUND));

            if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
                throw new IllegalStateException("Only confirmed reservations can be marked as no-show");
            }

            DiningTable table = reservation.getDiningTable();
            if (table != null) {
                table.setStatus(TableStatus.AVAILABLE);
                tableDb.save(table);
            }

            reservation.setStatus(ReservationStatus.NO_SHOW);
            reservation.setEndTime(LocalDateTime.now());
            Reservation savedReservation = reservationDb.save(reservation);

            return convertToDto(savedReservation);
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw e; // Re-throw these specific exceptions
        } catch (Exception e) {
            throw new IllegalStateException("Failed to mark reservation as no-show: " + e.getMessage());
        }
    }


    public boolean canModifyReservation(Reservation reservation) {
        return reservation.getStatus() != ReservationStatus.CANCELLED &&
                reservation.getStatus() != ReservationStatus.COMPLETED &&
                reservation.getReservationDate().isAfter(LocalDateTime.now());

    }
    public void validateReservationStatus(Reservation reservation, ReservationStatus expectedStatus, String action) {
        if (reservation.getStatus() != expectedStatus) {
            throw new IllegalStateException("Can only " + action + " " + expectedStatus.name().toLowerCase() + " reservations");
        }
    }


    private ReservationDto convertToDto(Reservation reservation) {
        ReservationDto dto = new ReservationDto();
        dto.setId(reservation.getId());
        dto.setUserId(reservation.getUser().getId());
        dto.setUserFirstName(reservation.getUser().getFirstName());
        dto.setUserLastName(reservation.getUser().getLastName());
        dto.setUserEmail(reservation.getUser().getEmail());
        dto.setUserPhoneNumber(reservation.getUser().getPhoneNumber());
        dto.setReservationDate(reservation.getReservationDate().toString());
        dto.setDurationMinutes(reservation.getDuration());
        dto.setNumberOfPeople(reservation.getNumberOfPeople());
        dto.setCompanyId(reservation.getCompany().getId());
        if (reservation.getCompany() != null) {
            dto.setCompanyName(reservation.getCompany().getName());
        }
        dto.setStatus(reservation.getStatus());
        if (reservation.getDiningTable() != null) {
            dto.setTableId(reservation.getDiningTable().getId());
            dto.setTableNumber(reservation.getDiningTable().getTableNumber());
        }
        return dto;
    }

    private Reservation convertToEntity(ReservationDto dto, User user) {
        if (dto == null) {
            throw new IllegalArgumentException("ReservationDto cannot be null");
        }
        if (dto.getReservationDate() == null || dto.getReservationDate().trim().isEmpty()) {
            throw new IllegalArgumentException("Reservation date cannot be null or empty");
        }
        if (dto.getNumberOfPeople() <= 0) {
            throw new IllegalArgumentException(NUMBER_PEOPLE_GREATER);
        }

        Reservation reservation = new Reservation();

        // Set user
        reservation.setUser(user);

        // Set basic details
        reservation.setReservationDate(LocalDateTime.parse(dto.getReservationDate()));
        reservation.setNumberOfPeople(dto.getNumberOfPeople());
        reservation.setSpecialRequests(dto.getSpecialRequests());

        // Set duration and calculate end time
        reservation.setDuration(dto.getDurationMinutes() != null ? dto.getDurationMinutes() : 120);
        reservation.setEndTime(reservation.getReservationDate().plusMinutes(reservation.getDuration()));

        // Set initial status
        reservation.setStatus(ReservationStatus.CONFIRMED);

        return reservation;
    }

    @Override
    public List<ReservationDto> getReservationsByTableAndDate(Long tableId, LocalDateTime startDate, LocalDateTime endDate) {

        DiningTable table = tableDb.findById(tableId)
                .orElseThrow(() -> new IllegalArgumentException("Table not found with id: " + tableId));

        return reservationDb.findByDiningTableAndReservationDateBetween(
                        table,
                        startDate,
                        endDate
                ).stream()
                .filter(reservation -> reservation.getStatus() != ReservationStatus.CANCELLED)
                .map(this::convertToDto)
                .toList();
    }


    @Override
    public List<ReservationDto> getReservationsByUser(User user) {
        return reservationDb.findByUser(user).stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public List<ReservationDto> getReservationsByCompany(UUID companyId) {
        Company company = findAndValidateCompany(companyId);
        return reservationDb.findByCompany(company).stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public List<ReservationDto> findByStatus(ReservationStatus status) {
        return reservationDb.findByStatus(status).stream()
                .map(this::convertToDto)
                .toList();
    }


    private void validateBasicReservationData(ReservationDto dto) {
        if (dto.getNumberOfPeople() <= 0) {
            throw new IllegalArgumentException(NUMBER_PEOPLE_GREATER);
        }

        if (dto.getCompanyId() == null) {
            throw new IllegalArgumentException("Company ID is required");
        }
    }



    private LocalDateTime parseAndValidateReservationTime(String dateTimeStr) {
        try {
            LocalDateTime reservationTime = LocalDateTime.parse(dateTimeStr);
            if (!reservationUtil.isValidReservationTime(reservationTime)) {
                throw new IllegalArgumentException("Invalid reservation time");
            }
            return reservationTime;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date format", e);
        }
    }





    private void validateReservationOwnership(Reservation reservation, User user) {
        if (!reservation.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Unauthorized access to reservation");
        }
    }

}
