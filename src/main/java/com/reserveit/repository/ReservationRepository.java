package com.reserveit.repository;

import com.reserveit.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
@Repository
public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    Optional<Reservation> findById(Long id);
}
