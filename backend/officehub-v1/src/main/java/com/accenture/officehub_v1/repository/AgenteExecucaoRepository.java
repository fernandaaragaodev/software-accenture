package com.accenture.officehub_v1.repository;

import com.accenture.officehub_v1.entity.AgenteExecucao;
import com.accenture.officehub_v1.entity.enums.StatusAgente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface AgenteExecucaoRepository extends JpaRepository<AgenteExecucao, UUID> {

    @Query("""
            SELECT a FROM AgenteExecucao a
            WHERE (:tipoAgente IS NULL OR a.tipoAgente = :tipoAgente)
              AND (:status IS NULL OR a.status = :status)
              AND (:dataInicio IS NULL OR a.createdAt >= :dataInicio)
              AND (:dataFim IS NULL OR a.createdAt <= :dataFim)
            ORDER BY a.createdAt DESC
            """)
    List<AgenteExecucao> buscarComFiltros(
            @Param("tipoAgente") String tipoAgente,
            @Param("status") StatusAgente status,
            @Param("dataInicio") OffsetDateTime dataInicio,
            @Param("dataFim") OffsetDateTime dataFim);
}
