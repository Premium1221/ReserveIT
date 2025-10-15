package com.reserveit.logic.impl;

import com.reserveit.enums.ReservationStatus;
import com.reserveit.enums.TableStatus;
import com.reserveit.model.DiningTable;
import com.reserveit.model.Reservation;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import com.reserveit.dto.TablePositionDto;
import com.reserveit.dto.ReservationDto;
import com.reserveit.logic.interfaces.DiningTableService;
import com.reserveit.logic.interfaces.ReservationService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class WebSocketService {
    private final SimpMessagingTemplate messagingTemplate;
    private final DiningTableService tableService;
    private final ReservationService reservationService;

    public WebSocketService(
            SimpMessagingTemplate messagingTemplate,
            DiningTableService tableService,
            ReservationService reservationService) {
        this.messagingTemplate = messagingTemplate;
        this.tableService = tableService;
        this.reservationService = reservationService;
    }

    public void notifyTableUpdate(Long tableId) {
        try {
            // Get the table position DTO directly from table service
            TablePositionDto tableDto = tableService.getTablePosition(tableId);

            if (tableDto != null) {
                // Get today's reservations for this table
                List<ReservationDto> todayReservations = reservationService.getReservationsByTableAndDate(
                        tableId,
                        LocalDateTime.now().withHour(0).withMinute(0),
                        LocalDateTime.now().withHour(23).withMinute(59)
                );

                // Update table status based on current time and reservations
                updateTableStatus(tableDto, todayReservations);

                // Set the reservations directly since we already have ReservationDtos
                tableDto.setReservations(todayReservations);

                // Send the update to all connected clients
                messagingTemplate.convertAndSend(
                        "/topic/tables/" + tableDto.getCompanyId(),
                        tableDto
                );
            }
        } catch (Exception e) {
            System.err.println("Error notifying table update: " + e.getMessage());
        }
    }

    private void updateTableStatus(TablePositionDto table, List<ReservationDto> reservations) {
        LocalDateTime now = LocalDateTime.now();

        // Check if there's an active reservation right now
        boolean hasCurrentReservation = reservations.stream()
                .anyMatch(reservation -> {
                    LocalDateTime reservationTime = LocalDateTime.parse(reservation.getReservationDate());
                    LocalDateTime endTime = reservation.getEndTime();
                    return now.isAfter(reservationTime) &&
                            now.isBefore(endTime) &&
                            reservation.getStatus() != ReservationStatus.CANCELLED;
                });

        // Check if there's an upcoming reservation in the next hour
        boolean hasUpcomingReservation = reservations.stream()
                .anyMatch(reservation -> {
                    LocalDateTime reservationTime = LocalDateTime.parse(reservation.getReservationDate());
                    return now.isBefore(reservationTime) &&
                            now.plusHours(1).isAfter(reservationTime) &&
                            reservation.getStatus() == ReservationStatus.CONFIRMED;
                });

        // Update the table status
        if (hasCurrentReservation) {
            table.setStatus(TableStatus.OCCUPIED);
        } else if (hasUpcomingReservation) {
            table.setStatus(TableStatus.RESERVED);
        } else {
            table.setStatus(TableStatus.AVAILABLE);
        }
    }
}