package com.accenture.officehub_v1.repository;

import com.accenture.officehub_v1.entity.Reserva;
import com.accenture.officehub_v1.entity.enums.StatusReserva;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservaRepository extends JpaRepository<Reserva, UUID> {

    Optional<Reserva> findByIdAndDeletedAtIsNull(UUID id);

    List<Reserva> findBySalaIdAndDataReservaAndDeletedAtIsNullAndStatusIn(
            UUID salaId, LocalDate dataReserva, Collection<StatusReserva> statuses);
}
