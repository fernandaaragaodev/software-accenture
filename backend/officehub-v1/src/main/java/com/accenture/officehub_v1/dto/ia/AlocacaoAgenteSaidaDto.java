package com.accenture.officehub_v1.dto.ia;

import java.util.List;

public record AlocacaoAgenteSaidaDto(
        boolean sucesso,
        String motivoFalha,
        String avisoProximidade,
        List<PosicaoAlocadaSaidaDto> alocacoes
) {
}
