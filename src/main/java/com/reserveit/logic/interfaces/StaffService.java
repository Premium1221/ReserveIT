package com.reserveit.logic.interfaces;

import com.reserveit.dto.UserDto;
import com.reserveit.model.Staff;

import java.util.UUID;

public interface StaffService  {
    Staff createStaffMember(UserDto userDto, UUID companyId, String rawPassword);
    Staff createManager(UserDto userDto, UUID companyId, String rawPassword);
    UUID getRelatedCompany(UUID staffId);

}
