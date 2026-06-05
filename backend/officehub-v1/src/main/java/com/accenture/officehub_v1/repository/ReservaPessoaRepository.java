package com.accenture.officehub_v1.repository;

import com.accenture.officehub_v1.entity.ReservaPessoa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ReservaPessoaRepository extends JpaRepository<ReservaPessoa, UUID> {

    @Query("""
            SELECT CASE WHEN COUNT(rp) > 0 THEN true ELSE false END
            FROM ReservaPessoa rp
            JOIN rp.usuario u
            JOIN EquipeMembro em ON em.usuario.id = u.id
            JOIN em.equipe e
            JOIN e.gestores eg
            WHERE rp.reserva.id = :reservaId
              AND eg.usuario.id = :gestorId
              AND e.deletedAt IS NULL
              AND u.deletedAt IS NULL
            """)
    boolean existsParticipanteNaEquipeDoGestor(
            @Param("reservaId") UUID reservaId,
            @Param("gestorId") UUID gestorId);
}
