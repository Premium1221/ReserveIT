package com.reserveit.database.impl;

import com.reserveit.database.interfaces.StaffDatabase;
import com.reserveit.model.Staff;
import com.reserveit.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class StaffDatabaseImpl implements StaffDatabase {

    private final StaffRepository staffRepository;

    @Override
    public UUID getCompanyIdByStaffId(UUID staffId) {
        return staffRepository.findCompanyIdByStaffId(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff member does not belong to any company"));
    }
}
