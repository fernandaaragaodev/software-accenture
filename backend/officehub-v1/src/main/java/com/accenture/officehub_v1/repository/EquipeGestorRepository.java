package com.accenture.officehub_v1.repository;

import com.accenture.officehub_v1.entity.EquipeGestor;
import com.accenture.officehub_v1.entity.EquipeGestorId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EquipeGestorRepository extends JpaRepository<EquipeGestor, EquipeGestorId> {

    boolean existsByEquipeIdAndUsuarioId(UUID equipeId, UUID usuarioId);

    List<EquipeGestor> findByEquipeId(UUID equipeId);

    void deleteByEquipeId(UUID equipeId);
}
