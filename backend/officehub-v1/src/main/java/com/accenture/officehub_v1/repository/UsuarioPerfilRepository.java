package com.accenture.officehub_v1.repository;

import com.accenture.officehub_v1.entity.UsuarioPerfil;
import com.accenture.officehub_v1.entity.UsuarioPerfilId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioPerfilRepository extends JpaRepository<UsuarioPerfil, UsuarioPerfilId> {
}
