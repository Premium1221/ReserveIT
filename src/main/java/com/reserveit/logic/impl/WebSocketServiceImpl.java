package com.reserveit.logic.impl;


import com.reserveit.logic.interfaces.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import com.reserveit.dto.TablePositionDto;
import com.reserveit.dto.ReservationDto;
import com.reserveit.logic.interfaces.DiningTableService;

import java.util.Map;
import java.util.UUID;


@Slf4j
@Service
public class WebSocketServiceImpl implements WebSocketService {
    private final SimpMessagingTemplate messagingTemplate;
    private final DiningTableService diningTableService;

    public WebSocketServiceImpl(SimpMessagingTemplate messagingTemplate,
                                DiningTableService diningTableService) {
        this.messagingTemplate = messagingTemplate;
        this.diningTableService = diningTableService;
    }

    public void notifyTableUpdate(Long tableId) {
        try {
            TablePositionDto tableDto = diningTableService.getTablePosition(tableId);
            if (tableDto != null && tableDto.getCompanyId() != null) {
                String destination = "/topic/tables/" + tableDto.getCompanyId();
                messagingTemplate.convertAndSend(destination, tableDto);

            }
        } catch (Exception e) {
            log.error("Error notifying table update: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendStaffNotification(UUID companyId, Map<String, Object> notification) {
        try {
            if (companyId != null) {
                String destination = "/topic/notifications/" + companyId;
                messagingTemplate.convertAndSend(destination, notification);
            }
        } catch (Exception e) {
            log.error("Error sending notification: {}", e.getMessage());
            e.printStackTrace();
        }
    }
    public void notifyReservationUpdate(ReservationDto reservation) {
        try {
            if (reservation != null && reservation.getCompanyId() != null) {
                String destination = "/topic/reservations/" + reservation.getCompanyId();
                messagingTemplate.convertAndSend(destination, reservation);
            }
        } catch (Exception e) {
            log.error("Error notifying reservation update: {}", e.getMessage());
        }
    }
}