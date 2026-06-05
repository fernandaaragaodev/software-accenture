package com.accenture.officehub_v1.dto.response;

import com.accenture.officehub_v1.entity.AgenteExecucao;
import com.accenture.officehub_v1.entity.enums.StatusAgente;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AgenteExecucaoResponse(
        UUID id,
        String tipoAgente,
        UUID referenciaId,
        StatusAgente status,
        String versaoModelo,
        Integer tempoProcessamentoMs,
        String erroMensagem,
        JsonNode payloadEntrada,
        JsonNode payloadSaida,
        OffsetDateTime executadoEm
) {

    public static AgenteExecucaoResponse from(AgenteExecucao execucao) {
        return new AgenteExecucaoResponse(
                execucao.getId(),
                execucao.getTipoAgente(),
                execucao.getReferenciaId(),
                execucao.getStatus(),
                execucao.getVersaoModelo(),
                execucao.getTempoProcessamentoMs(),
                execucao.getErroMensagem(),
                execucao.getPayloadEntrada(),
                execucao.getPayloadSaida(),
                execucao.getCreatedAt()
        );
    }
}
