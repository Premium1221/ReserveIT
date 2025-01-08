package com.reserveit.repository;

import com.reserveit.model.Staff;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import java.util.Optional;
import java.util.UUID;

public interface StaffRepository extends CrudRepository<Staff, UUID> {
    @Query("SELECT s.company.id FROM Staff s WHERE s.id = :staffId")
    Optional<UUID> findCompanyIdByStaffId(UUID staffId);
}