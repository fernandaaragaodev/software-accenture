package com.accenture.officehub.officehub_api.repository;

import com.accenture.officehub.officehub_api.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservaRepository extends JpaRepository<Reserva, String> {
}