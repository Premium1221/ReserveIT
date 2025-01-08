package com.reserveit.enums;

import lombok.Getter;

@Getter
public enum ReservationStatus {
    CONFIRMED("Confirmed"),
    CANCELLED("Cancelled"),
    COMPLETED("Completed"),
    NO_SHOW("No Show"),
    ARRIVED("Arrived");

    private final String displayName;

    ReservationStatus(String displayName) {
        this.displayName = displayName;
    }
}
