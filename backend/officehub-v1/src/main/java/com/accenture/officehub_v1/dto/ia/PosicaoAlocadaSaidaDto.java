package com.accenture.officehub_v1.dto.ia;

import java.util.UUID;

public record PosicaoAlocadaSaidaDto(
        UUID posicaoId,
        String identificador,
        String tipo,
        UUID usuarioId,
        String nomeExterno
) {
}
