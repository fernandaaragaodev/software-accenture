package com.accenture.officehub_v1.repository;

import com.accenture.officehub_v1.entity.EquipeMembro;
import com.accenture.officehub_v1.entity.EquipeMembroId;
import com.accenture.officehub_v1.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface EquipeMembroRepository extends JpaRepository<EquipeMembro, EquipeMembroId> {

    boolean existsByEquipeIdAndUsuarioId(UUID equipeId, UUID usuarioId);

    List<EquipeMembro> findByEquipeId(UUID equipeId);

    void deleteByEquipeIdAndUsuarioId(UUID equipeId, UUID usuarioId);

    @Query("""
            SELECT CASE WHEN COUNT(em) > 0 THEN true ELSE false END
            FROM EquipeMembro em
            JOIN em.equipe e
            JOIN e.gestores eg
            WHERE em.usuario.id = :membroId
              AND eg.usuario.id = :gestorId
              AND e.deletedAt IS NULL
            """)
    boolean existsMembroNaEquipeDoGestor(
            @Param("membroId") UUID membroId,
            @Param("gestorId") UUID gestorId);

    @Query("""
            SELECT CASE WHEN COUNT(em) > 0 THEN true ELSE false END
            FROM EquipeMembro em
            JOIN em.equipe e
            WHERE em.usuario.id = :usuarioId
              AND e.deletedAt IS NULL
              AND e.id <> :equipeId
            """)
    boolean existsEmOutraEquipeAtiva(
            @Param("usuarioId") UUID usuarioId,
            @Param("equipeId") UUID equipeId);

    @Query("""
            SELECT DISTINCT u
            FROM EquipeMembro em
            JOIN em.usuario u
            JOIN em.equipe e
            JOIN e.gestores eg
            WHERE eg.usuario.id = :gestorId
              AND e.deletedAt IS NULL
            ORDER BY u.nome ASC
            """)
    List<Usuario> findMembrosPorGestor(@Param("gestorId") UUID gestorId);

    @Query("""
            SELECT CASE WHEN COUNT(em) > 0 THEN true ELSE false END
            FROM EquipeMembro em
            JOIN em.equipe e
            JOIN e.gestores eg
            WHERE em.equipe.id = :equipeId
              AND em.usuario.id = :membroId
              AND eg.usuario.id = :gestorId
              AND e.deletedAt IS NULL
            """)
    boolean existsMembroNaEquipeDoGestor(
            @Param("equipeId") UUID equipeId,
            @Param("membroId") UUID membroId,
            @Param("gestorId") UUID gestorId);
}
