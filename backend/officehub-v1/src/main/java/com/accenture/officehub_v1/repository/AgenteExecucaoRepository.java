package com.accenture.officehub_v1.repository;

import com.accenture.officehub_v1.entity.AgenteExecucao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AgenteExecucaoRepository extends JpaRepository<AgenteExecucao, UUID> {
}
