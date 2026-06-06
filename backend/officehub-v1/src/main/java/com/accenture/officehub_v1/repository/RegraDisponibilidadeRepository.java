package com.accenture.officehub_v1.repository;

import com.accenture.officehub_v1.entity.RegraDisponibilidade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface RegraDisponibilidadeRepository extends JpaRepository<RegraDisponibilidade, UUID> {

    Optional<RegraDisponibilidade> findBySalaId(UUID salaId);

    boolean existsBySalaId(UUID salaId);

    List<RegraDisponibilidade> findAllByOrderByNomeAsc();

    Optional<RegraDisponibilidade> findByIdAndSalaIsNull(UUID id);
}
