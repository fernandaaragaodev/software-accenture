package com.accenture.officehub_v1.repository;

import com.accenture.officehub_v1.entity.TipoEquipamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TipoEquipamentoRepository extends JpaRepository<TipoEquipamento, UUID> {

    Optional<TipoEquipamento> findByIdAndAtivoTrue(UUID id);

    List<TipoEquipamento> findAllByOrderByNomeAsc();

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, UUID id);

    long count();

    long countByAtivoTrue();
}
