package com.reserveit.controller;

import com.reserveit.dto.ReservationDto;
import com.reserveit.enums.ReservationStatus;
import com.reserveit.logic.impl.WebSocketServiceImpl;
import com.reserveit.logic.interfaces.ReservationService;
import com.reserveit.logic.interfaces.WebSocketService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/staff/reservations")
@PreAuthorize("hasRole('STAFF') or hasRole('MANAGER')")
public class StaffReservationController {
    private final ReservationService reservationService;
    private final WebSocketService webSocketService;

    public StaffReservationController(ReservationService reservationService,
                                      WebSocketServiceImpl webSocketService) {
        this.reservationService = reservationService;
        this.webSocketService = webSocketService;
    }

    @PostMapping("/{id}/check-in")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER')")  // Add this annotation
    public ResponseEntity<Object> checkInReservation(@PathVariable Long id) {
        try {
            // Validate input
            if (id == null) {
                return ResponseEntity.badRequest().body("Reservation ID is required");
            }

            // Call service method
            ReservationDto dto = reservationService.checkInReservation(id);

            // Only notify if check-in was successful
            if (dto != null && dto.getTableId() != null) {
                webSocketService.notifyTableUpdate(dto.getTableId());
            }

            return ResponseEntity.ok(dto);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Check-in failed: " + e.getMessage());
        }
    }



    @PostMapping("/{id}/check-out")
    public ResponseEntity<Object> checkOutReservation(@PathVariable Long id) {
        try {
            ReservationDto dto = reservationService.checkOutReservation(id);

            // Send WebSocket notification about table update
            if (dto.getTableId() != null) {
                webSocketService.notifyTableUpdate(dto.getTableId());

                // Also send a general update notification
                Map<String, Object> notification = new HashMap<>();
                notification.put("type", "TABLE_STATUS_CHANGED");
                notification.put("tableId", dto.getTableId());
                notification.put("status", "AVAILABLE");
                webSocketService.sendStaffNotification(dto.getCompanyId(), notification);
            }

            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to check out: " + e.getMessage());
        }
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<ReservationDto>> getUpcomingReservations() {
        LocalDateTime now = LocalDateTime.now();
        return ResponseEntity.ok(reservationService.getReservationsByTimeRange(now, now.plusHours(24)));
    }
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ReservationDto>> getReservationsByStatus(@PathVariable String status) {
        try {
            ReservationStatus reservationStatus = ReservationStatus.valueOf(status.toUpperCase());
            List<ReservationDto> reservations = reservationService.findByStatus(reservationStatus);
            return ResponseEntity.ok(reservations);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<ReservationDto>> getReservationsByCompany(@PathVariable UUID companyId) {
        try {
            List<ReservationDto> reservations = reservationService.getReservationsByCompany(companyId);
            return ResponseEntity.ok(reservations);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    @GetMapping("/pending-arrival/{restaurantId}")
    public ResponseEntity<List<ReservationDto>> getPendingArrivalReservations(
            @PathVariable UUID restaurantId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime checkWindow = now.minusMinutes(15); // Check reservations from 15 minutes ago

        return ResponseEntity.ok(reservationService.getReservationsForArrivalCheck(
                restaurantId,
                checkWindow,
                now.plusMinutes(15)
        ));
    }

    @PostMapping("/{id}/no-show")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER')")
    public ResponseEntity<Object> markAsNoShow(@PathVariable Long id) {
        try {
            ReservationDto dto = reservationService.markAsNoShow(id);

            // Send WebSocket notifications
            if (dto.getTableId() != null) {
                // Notify about table update
                webSocketService.notifyTableUpdate(dto.getTableId());

                // Send status change notification
                Map<String, Object> notification = new HashMap<>();
                notification.put("type", "TABLE_STATUS_CHANGED");
                notification.put("tableId", dto.getTableId());
                notification.put("status", "AVAILABLE");
                webSocketService.sendStaffNotification(dto.getCompanyId(), notification);
            }

            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to mark as no-show: " + e.getMessage());
        }
    }

    @GetMapping("/extended-stay/{restaurantId}")
    public ResponseEntity<List<ReservationDto>> getExtendedStayReservations(
            @PathVariable UUID restaurantId) {
        return ResponseEntity.ok(
                reservationService.getExtendedStayReservations(restaurantId)
        );
    }

}