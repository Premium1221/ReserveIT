package com.reserveit.util;

import com.reserveit.dto.ReservationDto;
import com.reserveit.enums.ReservationStatus;
import com.reserveit.enums.TableStatus;
import com.reserveit.logic.interfaces.DiningTableService;
import com.reserveit.logic.interfaces.ReservationService;
import com.reserveit.logic.interfaces.WebSocketService;
import com.reserveit.model.DiningTable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class ReservationScheduler {
    private final ReservationService reservationService;
    private final WebSocketService webSocketService;
    private final DiningTableService tableService;

    public ReservationScheduler(
            ReservationService reservationService,
            WebSocketService webSocketService,
            DiningTableService tableService
    ) {
        this.reservationService = reservationService;
        this.webSocketService = webSocketService;
        this.tableService = tableService;
    }

    @Scheduled(fixedRate = 60000)
    public void checkAndNotifyLateArrivals() {
        List<Map<String, Object>> notifications = reservationService.checkForLateArrivals();

        for (Map<String, Object> notification : notifications) {
            UUID companyId = (UUID) notification.get("companyId");
            webSocketService.sendStaffNotification(companyId, notification);
        }
    }
    @Scheduled(fixedRate = 60000)
    public void checkAndUpdateReservations() {
        LocalDateTime now = LocalDateTime.now();

        // Get all upcoming reservations for next hour
        List<ReservationDto> upcomingReservations = reservationService.getReservationsByTimeRange(
                now,
                now.plusHours(1)
        );

        for (ReservationDto reservation : upcomingReservations) {
            LocalDateTime reservationTime = LocalDateTime.parse(reservation.getReservationDate());

            if (reservationTime.minusMinutes(30).isBefore(now) &&
                    reservation.getStatus() == ReservationStatus.CONFIRMED) {

                try {
                    DiningTable table = tableService.findById(reservation.getTableId());

                    if (table.getStatus() == TableStatus.AVAILABLE &&
                            !hasActiveReservation(table)) {

                        tableService.updateTableStatus(table.getId(), TableStatus.RESERVED);

                        Map<String, Object> notification = new HashMap<>();
                        notification.put("type", "UPCOMING_RESERVATION");
                        notification.put("message", "Table " + table.getTableNumber() +
                                " has an upcoming reservation in 30 minutes");
                        notification.put("reservationId", reservation.getId());

                        webSocketService.sendStaffNotification(
                                reservation.getCompanyId(),
                                notification
                        );
                    }
                } catch (Exception e) {
                    log.error("Error processing reservation: {}", e.getMessage());
                }
            }
        }
    }
    private boolean hasActiveReservation(DiningTable table) {
        return table.getReservations().stream()
                .anyMatch(reservation ->
                        reservation.getStatus() == ReservationStatus.ARRIVED &&
                                reservation.getEndTime().isAfter(LocalDateTime.now())
                );
    }
}
