package com.accenture.officehub_v1.repository;

import com.accenture.officehub_v1.entity.UsuarioPerfil;
import com.accenture.officehub_v1.entity.UsuarioPerfilId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UsuarioPerfilRepository extends JpaRepository<UsuarioPerfil, UsuarioPerfilId> {

    @Query("SELECT up FROM UsuarioPerfil up JOIN FETCH up.perfil WHERE up.usuario.id = :usuarioId")
    List<UsuarioPerfil> findByUsuarioId(@Param("usuarioId") UUID usuarioId);
}
