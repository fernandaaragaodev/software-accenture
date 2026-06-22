package com.accenture.officehub_v1.dto.response;

import java.util.List;

public record GerarLayoutPorIaResponse(
        SalaResponse sala,
        LayoutResponse layout,
        List<PosicaoComEquipamentosResponse> posicoes,
        int totalDetecoes,
        int totalEstacoes
) {
}
