package com.reserveit.logic.impl;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import com.reserveit.dto.TablePositionDto;
import com.reserveit.dto.ReservationDto;
import com.reserveit.logic.interfaces.DiningTableService;
import com.reserveit.logic.interfaces.ReservationService;

import java.util.UUID;

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

    /**
     * Notify clients about table updates
     */
    public void notifyTableUpdate(Long tableId) {
        try {
            TablePositionDto updatedTable = tableService.getTablePosition(tableId);
            if (updatedTable != null) {
                messagingTemplate.convertAndSend(
                        "/topic/tables/" + updatedTable.getCompanyId(),
                        updatedTable
                );
            }
        } catch (Exception e) {
            System.err.println("Error notifying table update: " + e.getMessage());
        }
    }

    /**
     * Notify clients about reservation updates
     */
    public void notifyReservationUpdate(Long reservationId) {
        try {
            ReservationDto reservation = reservationService.getReservationById(reservationId);
            if (reservation != null) {
                messagingTemplate.convertAndSend(
                        "/topic/reservations/" + reservation.getCompanyId(),
                        reservation
                );
            }
        } catch (Exception e) {
            System.err.println("Error notifying reservation update: " + e.getMessage());
        }
    }

    /**
     * Send company-wide notification
     */
    public void broadcastToCompany(UUID companyId, String message) {
        try {
            messagingTemplate.convertAndSend(
                    "/topic/notifications/" + companyId,
                    message
            );
        } catch (Exception e) {
            System.err.println("Error broadcasting message: " + e.getMessage());
        }
    }
}