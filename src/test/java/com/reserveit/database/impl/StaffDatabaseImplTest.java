package com.reserveit.database.impl;

import com.reserveit.repository.StaffRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StaffDatabaseImplTest {

    @Mock
    private StaffRepository staffRepository;

    @InjectMocks
    private StaffDatabaseImpl db;

    @Test
    void getCompanyIdByStaffId_success(){
        UUID staffId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        when(staffRepository.findCompanyIdByStaffId(staffId)).thenReturn(Optional.of(companyId));
        assertEquals(companyId, db.getCompanyIdByStaffId(staffId));
    }

    @Test
    void getCompanyIdByStaffId_notFound_throws(){
        UUID staffId = UUID.randomUUID();
        when(staffRepository.findCompanyIdByStaffId(staffId)).thenReturn(Optional.empty());
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> db.getCompanyIdByStaffId(staffId));
        assertTrue(ex.getMessage().contains("Staff member does not belong"));
    }
}

