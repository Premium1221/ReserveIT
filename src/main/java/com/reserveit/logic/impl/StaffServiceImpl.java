package com.reserveit.logic.impl;

import com.reserveit.database.interfaces.CompanyDatabase;
import com.reserveit.database.interfaces.StaffDatabase;
import com.reserveit.database.interfaces.UserDatabase;
import com.reserveit.dto.UserDto;
import com.reserveit.enums.UserRole;
import com.reserveit.logic.interfaces.StaffService;
import com.reserveit.model.Company;
import com.reserveit.model.Staff;
import com.reserveit.util.PasswordHasher;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class StaffServiceImpl implements StaffService {
    private final UserDatabase userDb;
    private final CompanyDatabase companyDb;
    private final PasswordHasher passwordHasher;
    private final StaffDatabase staffDb;

    public StaffServiceImpl(UserDatabase userDb,
                            CompanyDatabase companyDb,
                            PasswordHasher passwordHasher,
                            StaffDatabase staffDb) {
        this.userDb = userDb;
        this.companyDb = companyDb;
        this.passwordHasher = passwordHasher;
        this.staffDb = staffDb;
    }

    @Transactional
    public Staff createStaffMember(UserDto userDto, UUID companyId, String rawPassword) {
        Staff staff = new Staff(UserRole.STAFF);
        populateStaffData(staff, userDto, rawPassword);

        Company company = companyDb.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));
        staff.setCompany(company);

        return (Staff) userDb.save(staff);
    }

    @Transactional
    public Staff createManager(UserDto userDto, UUID companyId, String rawPassword) {
        try {
            Staff manager = new Staff(UserRole.MANAGER);

            populateStaffData(manager, userDto, rawPassword);


            Company company = companyDb.findById(companyId)
                    .orElseThrow(() -> new IllegalArgumentException("Company not found"));
            manager.setCompany(company);


            return (Staff) userDb.save(manager);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create manager: " + e.getMessage());
        }
    }

    private void populateStaffData(Staff staff, UserDto userDto, String rawPassword) {
        staff.setFirstName(userDto.getFirstName());
        staff.setLastName(userDto.getLastName());
        staff.setEmail(userDto.getEmail());
        staff.setPhoneNumber(userDto.getPhoneNumber());
        staff.setHashedPassword(passwordHasher.hashPassword(rawPassword));
    }

    @Override
    public UUID getRelatedCompany(UUID staffId) {
        return staffDb.getCompanyIdByStaffId(staffId);
    }

}

