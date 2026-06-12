package com.accenture.officehub_v1.service.ia.motor;

import com.accenture.officehub_v1.dto.ia.AlocacaoAgenteSaidaDto;

import java.util.Optional;

public record ResultadoChamadaOpenRouter(
        AlocacaoAgenteSaidaDto saida,
        Optional<String> erroValidacao,
        int tempoMs
) {
}
