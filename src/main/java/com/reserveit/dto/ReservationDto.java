package com.reserveit.dto;

import com.reserveit.enums.ReservationStatus;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

public class ReservationDto {
    @Setter
    @Getter
    private Long id;

    @Setter
    @Getter
    @NotBlank(message = "Customer name is required")
    private String customerName;

    @Setter
    @Getter
    @NotBlank(message = "Customer email is required")
    @Email(message = "Invalid email format")
    private String customerEmail;

    @Setter
    @Getter
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
    private String customerPhone;

    @Setter
    @Getter
    @NotNull(message = "Reservation date is required")
    private String reservationDate;

    // Add duration in minutes (default 2 hours = 120 minutes)
    @Setter
    @Getter
    @Min(value = 30, message = "Minimum duration is 30 minutes")
    @Max(value = 240, message = "Maximum duration is 4 hours")
    private int durationMinutes = 120;

    @Setter
    @Getter
    @Min(value = 1, message = "Number of people must be at least 1")
    private int numberOfPeople;

    @Setter
    @Getter
    @NotNull(message = "Company ID is required")
    private UUID companyId;

    @Setter
    @Getter
    private String companyName;

    @Setter
    @Getter
    private Long tableId;

    @Setter
    @Getter
    private String tableNumber;

    @Setter
    @Getter
    private ReservationStatus status;

    @Setter
    @Getter
    private String specialRequests;

    @Setter
    @Getter
    private LocalDateTime checkedInAt;

    @Setter
    @Getter
    private LocalDateTime checkedOutAt;

    @Setter
    @Getter
    private Integer duration;

    @Setter
    @Getter
    private LocalDateTime endTime;
}
