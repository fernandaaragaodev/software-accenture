package com.accenture.officehub_v1.repository;

import com.accenture.officehub_v1.entity.ReservaPosicao;
import com.accenture.officehub_v1.entity.enums.StatusReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ReservaPosicaoRepository extends JpaRepository<ReservaPosicao, UUID> {

    @Query("""
            SELECT rp.posicao.id FROM ReservaPosicao rp
            WHERE rp.reserva.sala.id = :salaId
              AND rp.reserva.dataReserva = :data
              AND rp.reserva.deletedAt IS NULL
              AND rp.reserva.status IN :statuses
              AND rp.reserva.horaInicio < :horaFim
              AND rp.reserva.horaFim > :horaInicio
            """)
    List<UUID> findPosicaoIdsOcupadas(
            @Param("salaId") UUID salaId,
            @Param("data") LocalDate data,
            @Param("horaInicio") java.time.LocalTime horaInicio,
            @Param("horaFim") java.time.LocalTime horaFim,
            @Param("statuses") Collection<StatusReserva> statuses);

    @Query("""
            SELECT rp FROM ReservaPosicao rp
            JOIN FETCH rp.posicao
            JOIN FETCH rp.reservaPessoa rpessoa
            LEFT JOIN FETCH rpessoa.usuario
            WHERE rp.reserva.id = :reservaId
            ORDER BY rp.createdAt ASC
            """)
    List<ReservaPosicao> findByReservaIdWithDetails(@Param("reservaId") UUID reservaId);
}
