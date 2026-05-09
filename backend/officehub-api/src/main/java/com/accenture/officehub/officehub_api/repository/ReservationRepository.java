package com.accenture.officehub.officehub_api.repository;

import com.accenture.officehub.officehub_api.model.Reservation;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository {
    List<Reservation> findAll();
    Optional<Reservation> findById(Long id);
    Reservation save(Reservation reservation);
    void saveAll(List<Reservation> reservations);
}
