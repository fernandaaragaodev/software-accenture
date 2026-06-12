package com.accenture.officehub_v1.repository;

import com.accenture.officehub_v1.entity.UsuarioEspecialidade;
import com.accenture.officehub_v1.entity.UsuarioEspecialidadeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UsuarioEspecialidadeRepository extends JpaRepository<UsuarioEspecialidade, UsuarioEspecialidadeId> {

    List<UsuarioEspecialidade> findByUsuarioId(UUID usuarioId);

    @Modifying
    @Query("DELETE FROM UsuarioEspecialidade ue WHERE ue.usuario.id = :usuarioId")
    void deleteByUsuarioId(@Param("usuarioId") UUID usuarioId);
}
