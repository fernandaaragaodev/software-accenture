package com.accenture.officehub_v1.dto.response;

import java.util.List;

public record PosicaoComEquipamentosResponse(
        PosicaoResponse posicao,
        List<PosicaoEquipamentoResponse> equipamentos
) {
}
