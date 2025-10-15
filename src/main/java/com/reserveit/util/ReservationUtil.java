package com.reserveit.util;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ReservationUtil {
    public static int calculateDefaultDuration(LocalDateTime reservationTime) {
        int hour = reservationTime.getHour();

        // Morning (before 11 AM):
        if (hour < 11) {
            return 120;
        }
        // Lunch time (11 AM - 3 PM)
        else if (hour >= 11 && hour < 15) {
            return 180;
        }
        // Afternoon (3 PM - 5 PM)
        else if (hour >= 15 && hour < 17) {
            return 120;
        }
        // Evening/Dinner (5 PM onwards)
        else {
            return 360;
        }
    }
    private boolean isWithinBusinessHours(LocalDateTime reservationTime) {
        int hour = reservationTime.getHour();

        return hour >= 6 ;
    }

    public boolean isValidReservationTime(LocalDateTime reservationTime) {
        if (!isWithinBusinessHours(reservationTime)) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        return !reservationTime.isBefore(now);
    }
}

