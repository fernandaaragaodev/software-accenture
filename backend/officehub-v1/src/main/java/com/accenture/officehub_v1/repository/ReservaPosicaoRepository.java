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
            """)
    List<UUID> findPosicaoIdsOcupadas(
            @Param("salaId") UUID salaId,
            @Param("data") LocalDate data,
            @Param("statuses") Collection<StatusReserva> statuses);
}
