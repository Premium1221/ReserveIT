package com.reserveit.repository;

import com.reserveit.model.Reservation;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class HardcodedReservationRepository {

    private final List<Reservation> reservations = new ArrayList<>();
    private Long nextId = 1L;

    public Reservation save(Reservation reservation) {
        if (reservation.getId() == null) {
            reservation.setId(nextId++);
        } else {
            deleteById(reservation.getId());
        }
        reservations.add(reservation);
        return reservation;
    }

    public List<Reservation> findAll() {
        return new ArrayList<>(reservations);
    }

    public Optional<Reservation> findById(Long id) {
        return reservations.stream()
                .filter(reservation -> reservation.getId().equals(id))
                .findFirst();
    }

    public void deleteById(Long id) {
        reservations.removeIf(reservation -> reservation.getId().equals(id));
    }
}
