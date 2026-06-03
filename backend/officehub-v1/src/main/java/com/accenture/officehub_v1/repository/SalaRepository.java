package com.accenture.officehub_v1.repository;

import com.accenture.officehub_v1.entity.Sala;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SalaRepository extends JpaRepository<Sala, UUID> {

    List<Sala> findByDeletedAtIsNull();

    Optional<Sala> findByIdAndDeletedAtIsNull(UUID id);

    boolean existsByNomeIgnoreCaseAndDeletedAtIsNull(String nome);

    boolean existsByNomeIgnoreCaseAndDeletedAtIsNullAndIdNot(String nome, UUID id);
}
