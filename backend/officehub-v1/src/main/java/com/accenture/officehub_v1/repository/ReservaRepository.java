package com.accenture.officehub_v1.repository;

import com.accenture.officehub_v1.entity.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReservaRepository extends JpaRepository<Reserva, UUID> {
}
