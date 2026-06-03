package com.accenture.officehub_v1.repository;

import com.accenture.officehub_v1.entity.RegraDisponibilidade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RegraDisponibilidadeRepository extends JpaRepository<RegraDisponibilidade, UUID> {
}
