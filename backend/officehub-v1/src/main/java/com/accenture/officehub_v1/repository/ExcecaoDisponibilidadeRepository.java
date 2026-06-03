package com.accenture.officehub_v1.repository;

import com.accenture.officehub_v1.entity.ExcecaoDisponibilidade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface ExcecaoDisponibilidadeRepository extends JpaRepository<ExcecaoDisponibilidade, UUID> {

    boolean existsBySalaIdAndData(UUID salaId, LocalDate data);

    Optional<ExcecaoDisponibilidade> findBySalaIdAndData(UUID salaId, LocalDate data);
}
