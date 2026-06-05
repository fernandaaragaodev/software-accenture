package com.accenture.officehub_v1.repository;

import com.accenture.officehub_v1.entity.Equipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EquipeRepository extends JpaRepository<Equipe, UUID> {

    List<Equipe> findByDeletedAtIsNullOrderByNomeAsc();

    Optional<Equipe> findByIdAndDeletedAtIsNull(UUID id);

    @Query("""
            SELECT DISTINCT e FROM Equipe e
            JOIN e.gestores g
            WHERE g.usuario.id = :gestorId
              AND e.deletedAt IS NULL
            ORDER BY e.nome ASC
            """)
    List<Equipe> findAtivasPorGestor(@Param("gestorId") UUID gestorId);
}
