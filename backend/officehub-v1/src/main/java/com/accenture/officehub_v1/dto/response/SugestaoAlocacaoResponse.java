package com.accenture.officehub_v1.dto.response;

import java.util.List;
import java.util.UUID;

public record SugestaoAlocacaoResponse(
        UUID execucaoId,
        String avisoProximidade,
        List<ReservaPosicaoAlocadaResponse> alocacoes,
        List<UUID> posicoesSugeridas
) {
}
