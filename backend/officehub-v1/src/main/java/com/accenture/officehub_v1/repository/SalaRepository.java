package com.accenture.officehub_v1.repository;

import com.accenture.officehub_v1.entity.Sala;
import com.accenture.officehub_v1.entity.enums.StatusSala;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SalaRepository extends JpaRepository<Sala, UUID> {

    List<Sala> findByDeletedAtIsNull();

    List<Sala> findByDeletedAtIsNullAndStatusNot(StatusSala status);

    Optional<Sala> findByIdAndDeletedAtIsNull(UUID id);

    boolean existsByNomeIgnoreCaseAndDeletedAtIsNull(String nome);

    boolean existsByNomeIgnoreCaseAndDeletedAtIsNullAndIdNot(String nome, UUID id);

    long countByDeletedAtIsNull();
}
