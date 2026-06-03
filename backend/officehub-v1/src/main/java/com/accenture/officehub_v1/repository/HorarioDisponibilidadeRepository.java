package com.accenture.officehub_v1.repository;

import com.accenture.officehub_v1.entity.HorarioDisponibilidade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface HorarioDisponibilidadeRepository extends JpaRepository<HorarioDisponibilidade, UUID> {
}
