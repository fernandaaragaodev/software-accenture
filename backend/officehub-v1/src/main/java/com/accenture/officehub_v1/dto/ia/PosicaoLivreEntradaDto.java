package com.accenture.officehub_v1.dto.ia;

import java.math.BigDecimal;
import java.util.UUID;

public record PosicaoLivreEntradaDto(
        UUID id,
        String tipo,
        BigDecimal coordX,
        BigDecimal coordY
) {
}
