package com.reserveit.logic.interfaces;

import com.reserveit.dto.ReservationDto;
import java.util.Map;
import java.util.UUID;

public interface WebSocketService {
    void notifyTableUpdate(Long tableId);
    void sendStaffNotification(UUID companyId, Map<String, Object> notification);
    void notifyReservationUpdate(ReservationDto reservation);


    }
