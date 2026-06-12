package com.accenture.officehub_v1.repository;

import com.accenture.officehub_v1.entity.Reserva;
import com.accenture.officehub_v1.entity.enums.StatusReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservaRepository extends JpaRepository<Reserva, UUID> {

    Optional<Reserva> findByIdAndDeletedAtIsNull(UUID id);

    List<Reserva> findBySalaIdAndDataReservaAndDeletedAtIsNullAndStatusIn(
            UUID salaId, LocalDate dataReserva, Collection<StatusReserva> statuses);

    long countByDeletedAtIsNull();

    long countByDeletedAtIsNullAndStatus(StatusReserva status);

    @Query("""
            SELECT r FROM Reserva r
            JOIN FETCH r.sala s
            JOIN FETCH r.solicitante sol
            WHERE r.deletedAt IS NULL
              AND r.status IN :statuses
              AND r.dataReserva = :data
            ORDER BY r.dataReserva DESC, r.horaInicio ASC
            """)
    List<Reserva> findAtivasPorData(
            @Param("statuses") Collection<StatusReserva> statuses,
            @Param("data") LocalDate data);

    @Query("""
            SELECT r FROM Reserva r
            JOIN FETCH r.sala s
            JOIN FETCH r.solicitante sol
            WHERE r.deletedAt IS NULL
              AND r.status IN :statuses
            ORDER BY r.dataReserva DESC, r.horaInicio ASC
            """)
    List<Reserva> findAtivas(@Param("statuses") Collection<StatusReserva> statuses);

    @Query("""
            SELECT r FROM Reserva r
            JOIN FETCH r.sala s
            JOIN FETCH r.solicitante sol
            LEFT JOIN FETCH r.canceladoPor cp
            WHERE r.status = :status
              AND r.dataReserva = :data
            ORDER BY r.canceladoEm DESC
            """)
    List<Reserva> findCanceladasPorData(
            @Param("status") StatusReserva status,
            @Param("data") LocalDate data);

    @Query("""
            SELECT r FROM Reserva r
            JOIN FETCH r.sala s
            JOIN FETCH r.solicitante sol
            LEFT JOIN FETCH r.canceladoPor cp
            WHERE r.status = :status
            ORDER BY r.canceladoEm DESC
            """)
    List<Reserva> findCanceladas(@Param("status") StatusReserva status);

    @Query("""
            SELECT r FROM Reserva r
            JOIN FETCH r.sala s
            JOIN FETCH r.solicitante sol
            LEFT JOIN FETCH r.canceladoPor cp
            WHERE r.id = :id
            """)
    Optional<Reserva> findByIdComDetalhes(@Param("id") UUID id);
}
