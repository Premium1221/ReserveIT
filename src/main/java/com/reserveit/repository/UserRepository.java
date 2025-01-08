package com.reserveit.repository;

import com.reserveit.model.Staff;
import com.reserveit.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    User findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE TYPE(u) = Staff AND u.id = :id")
    Optional<Staff> findStaffById(@Param("id") UUID id);
//
}
