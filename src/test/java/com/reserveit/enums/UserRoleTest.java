package com.reserveit.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserRoleTest {

    @Test
    void roleHelpers_cover_all_paths(){
        assertTrue(UserRole.ADMIN.hasAdminAccess());
        assertTrue(UserRole.ADMIN.hasManagementAccess());
        assertTrue(UserRole.ADMIN.canManageReservations());

        assertTrue(UserRole.MANAGER.hasManagementAccess());
        assertTrue(UserRole.MANAGER.canManageReservations());

        assertTrue(UserRole.STAFF.canManageReservations());
        assertTrue(UserRole.STAFF.isStaff());

        assertTrue(UserRole.CUSTOMER.isCustomer());
        assertFalse(UserRole.CUSTOMER.canManageReservations());

        // valueOf sanity
        assertEquals(UserRole.ADMIN, UserRole.valueOf("ADMIN"));
        assertEquals(4, UserRole.values().length);
    }
}

