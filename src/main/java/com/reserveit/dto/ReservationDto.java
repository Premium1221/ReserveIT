package com.reserveit.dto;

import com.reserveit.enums.ReservationStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;
@Setter
@Getter
public class ReservationDto {
    private Long id;
    private UUID userId;
    private String userFirstName;
    private String userLastName;
    private String userEmail;
    private String userPhoneNumber;
    private String reservationDate;
    private Integer durationMinutes;
    private int numberOfPeople;
    private UUID companyId;
    private String companyName;
    private Long tableId;
    private String tableNumber;
    private ReservationStatus status;
    private String specialRequests;
    private LocalDateTime checkedInAt;
    private LocalDateTime checkedOutAt;
    private LocalDateTime endTime;
}