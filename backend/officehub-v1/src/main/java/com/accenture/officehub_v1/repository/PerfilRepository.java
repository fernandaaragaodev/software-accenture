package com.accenture.officehub_v1.repository;

import com.accenture.officehub_v1.entity.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PerfilRepository extends JpaRepository<Perfil, UUID> {
}
