package com.accenture.officehub_v1.repository;

import com.accenture.officehub_v1.entity.AgenteExecucao;
import com.accenture.officehub_v1.entity.enums.StatusAgente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface AgenteExecucaoRepository extends JpaRepository<AgenteExecucao, UUID> {

    List<AgenteExecucao> findByTipoAgenteAndStatusAndCreatedAtBetweenOrderByCreatedAtDesc(
            String tipoAgente,
            StatusAgente status,
            OffsetDateTime dataInicio,
            OffsetDateTime dataFim
    );

    List<AgenteExecucao> findByTipoAgenteAndCreatedAtBetweenOrderByCreatedAtDesc(
            String tipoAgente,
            OffsetDateTime dataInicio,
            OffsetDateTime dataFim
    );

    List<AgenteExecucao> findByStatusAndCreatedAtBetweenOrderByCreatedAtDesc(
            StatusAgente status,
            OffsetDateTime dataInicio,
            OffsetDateTime dataFim
    );

    List<AgenteExecucao> findByCreatedAtBetweenOrderByCreatedAtDesc(
            OffsetDateTime dataInicio,
            OffsetDateTime dataFim
    );
}
