package com.accenture.officehub.officehub_api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.accenture.officehub.officehub_api.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

	Optional<Usuario> findByEmail(String email);

	boolean existsByEmail(String email);
}
