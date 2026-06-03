package com.accenture.officehub_v1.repository;

import com.accenture.officehub_v1.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    Optional<Usuario> findByIdAndDeletedAtIsNull(UUID id);
}
