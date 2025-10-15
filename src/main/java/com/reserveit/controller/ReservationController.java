package com.reserveit.controller;

import com.reserveit.dto.ReservationDto;
import com.reserveit.enums.ReservationStatus;
import com.reserveit.logic.interfaces.ReservationService;
import com.reserveit.logic.interfaces.UserService;
import com.reserveit.logic.interfaces.WebSocketService;
import com.reserveit.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reservations")
@CrossOrigin(
        origins = {"http://localhost:5200", "http://127.0.0.1:5200", "http://172.29.96.1:5200"},
        allowCredentials = "true"
)
public class ReservationController {
    private final ReservationService reservationService;
    private final WebSocketService webSocketService;
    private final UserService userService;

    public ReservationController(ReservationService reservationService, WebSocketService webSocketService, UserService userService) {
        this.reservationService = reservationService;
        this.webSocketService = webSocketService;
        this.userService = userService;
    }

    private User getCurrentUser() {
        return userService.getCurrentUser();
    }

    @PostMapping
    public ResponseEntity<ReservationDto> createReservation(@RequestBody ReservationDto reservationDto) {
        try {
            User currentUser = getCurrentUser();
            ReservationDto savedReservation = reservationService.createReservation(reservationDto, currentUser);

            if (savedReservation.getTableId() != null) {
                webSocketService.notifyTableUpdate(savedReservation.getTableId());
            }

            return ResponseEntity.status(201).body(savedReservation);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationDto> getReservationById(@PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();
            ReservationDto reservation = reservationService.getReservationById(id, currentUser);
            return ResponseEntity.ok(reservation);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReservationDto> updateReservation(@PathVariable Long id, @RequestBody ReservationDto reservationDto) {
        try {
            User currentUser = getCurrentUser();
            ReservationDto updatedReservation = reservationService.updateReservation(id, reservationDto, currentUser);
            return ResponseEntity.ok(updatedReservation);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> cancelReservation(@PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();
            reservationService.cancelReservation(id, currentUser);
            return ResponseEntity.ok("Reservation cancelled successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/quick")
    public ResponseEntity<?> createQuickReservation(
            @RequestBody ReservationDto reservationDto,
            @RequestParam(defaultValue = "false") boolean immediate) {
        try {
            User currentUser = getCurrentUser();
            ReservationDto savedReservation = reservationService.createQuickReservation(
                    reservationDto,
                    immediate,
                    currentUser
            );

            if (savedReservation != null) {
                webSocketService.notifyReservationUpdate(savedReservation);
                if (savedReservation.getTableId() != null) {
                    webSocketService.notifyTableUpdate(savedReservation.getTableId());
                }
            }

            return ResponseEntity.ok(savedReservation);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("message", "Unexpected error creating reservation"));
        }
    }

    @GetMapping("/check-availability")
    public ResponseEntity<Boolean> checkTimeSlotAvailability(
            @RequestParam UUID companyId,
            @RequestParam Long tableId,
            @RequestParam String dateTime
    ) {
        try {
            boolean isAvailable = reservationService.isTimeSlotAvailable(
                    companyId, tableId, dateTime, null
            );
            return ResponseEntity.ok(isAvailable);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }

    }

    @GetMapping("/my-reservations")
    public ResponseEntity<List<ReservationDto>> getMyReservations() {
        try {
            User currentUser = userService.getCurrentUser();
            List<ReservationDto> reservations = reservationService.getReservationsByUser(currentUser);
            return ResponseEntity.ok(reservations);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<ReservationDto>> getAllReservations() {
        List<ReservationDto> reservations = reservationService.getAllReservations();
        return ResponseEntity.ok(reservations);
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

    @DeleteMapping("/all")
    public ResponseEntity<String> deleteAllReservations() {
        try {
            reservationService.deleteAllReservations();
            return ResponseEntity.ok("All reservations have been deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete reservations: " + e.getMessage());
        }
    }
}
