package com.reserveit.controller;

import com.reserveit.logic.interfaces.ReservationService;
import com.reserveit.logic.interfaces.WebSocketService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class ReservationScheduler {
    private final ReservationService reservationService;
    private final WebSocketService webSocketService;

    public ReservationScheduler(
            ReservationService reservationService,
            WebSocketService webSocketService
    ) {
        this.reservationService = reservationService;
        this.webSocketService = webSocketService;
    }

    @Scheduled(fixedRate = 60000)
    public void checkAndNotifyLateArrivals() {
        List<Map<String, Object>> notifications = reservationService.checkForLateArrivals();

        for (Map<String, Object> notification : notifications) {
            UUID companyId = (UUID) notification.get("companyId");
            webSocketService.sendStaffNotification(companyId, notification);
        }
    }
}
