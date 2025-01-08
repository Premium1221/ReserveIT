package com.reserveit.database.interfaces;

import java.util.UUID;

public interface StaffDatabase {
    UUID getCompanyIdByStaffId(UUID staffId);
}
