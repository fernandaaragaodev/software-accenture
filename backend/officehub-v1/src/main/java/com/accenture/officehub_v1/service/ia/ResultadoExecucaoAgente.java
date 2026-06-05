package com.accenture.officehub_v1.service.ia;

import com.accenture.officehub_v1.service.alocacao.ResultadoAlocacao;

import java.util.UUID;

public record ResultadoExecucaoAgente(
        ResultadoAlocacao resultadoAlocacao,
        UUID execucaoId
) {
}
