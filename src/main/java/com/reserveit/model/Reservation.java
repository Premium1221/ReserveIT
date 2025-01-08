package com.reserveit.model;

import com.reserveit.enums.TableStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.reserveit.enums.ReservationStatus;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Getter
@Setter
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Customer name is required")
    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @NotNull(message = "Email is required")
    @Email(message = "Invalid email format")
    @Column(name = "customer_email")
    private String customerEmail;

    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
    @Column(name = "customer_phone")
    private String customerPhone;

    @NotNull(message = "Reservation date is required")
    @Future(message = "Reservation date must be in the future")
    @Column(name = "reservation_date", nullable = false)
    private LocalDateTime reservationDate;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Min(value = 60, message = "Duration must be at least 60 minutes")
    @Max(value = 240, message = "Duration cannot exceed 240 minutes")
    @Column(name = "duration", nullable = false)
    private Integer duration = 120; // Default 2 hours

    @Min(value = 1, message = "Number of people must be at least 1")
    @Column(name = "number_of_people", nullable = false)
    private int numberOfPeople;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dining_table_id")
    private DiningTable diningTable;

    @Column(name = "special_requests")
    @Size(max = 500, message = "Special requests cannot exceed 500 characters")
    private String specialRequests;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status = ReservationStatus.CONFIRMED;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "check_in_time")
    private LocalDateTime checkInTime;

    @Column(name = "check_out_time")
    private LocalDateTime checkOutTime;

    // Helper methods
    public boolean canCheckIn() {
        LocalDateTime now = LocalDateTime.now();
        return status == ReservationStatus.CONFIRMED &&
                Math.abs(now.getMinute() - reservationDate.getMinute()) <= 15;
    }

    public boolean canExtendTime() {
        return status == ReservationStatus.ARRIVED;
    }

    public void extend(int additionalMinutes) {
        this.duration += additionalMinutes;
        this.endTime = this.endTime.plusMinutes(additionalMinutes);
    }

    public void checkIn() {
        if (!canCheckIn()) {
            throw new IllegalStateException("Cannot check in at this time");
        }
        this.status = ReservationStatus.ARRIVED;
        this.checkInTime = LocalDateTime.now();
        if (this.diningTable != null) {
            this.diningTable.setStatus(TableStatus.OCCUPIED);
        }
    }

    public void checkOut() {
        if (this.status != ReservationStatus.ARRIVED) {
            throw new IllegalStateException("Can only check out arrived reservations");
        }
        this.status = ReservationStatus.COMPLETED;
        this.checkOutTime = LocalDateTime.now();
        if (this.diningTable != null) {
            this.diningTable.setStatus(TableStatus.AVAILABLE);
        }
    }
}