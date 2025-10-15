package com.reserveit.enums;

public enum TableStatus {
    AVAILABLE,
    OCCUPIED,
    RESERVED,
    OUT_OF_SERVICE,
    CLEANING;

    public String getStatus() {
        return this.name();
    }
}
