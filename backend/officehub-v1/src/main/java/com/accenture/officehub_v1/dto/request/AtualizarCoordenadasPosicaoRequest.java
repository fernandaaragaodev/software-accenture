package com.accenture.officehub_v1.dto.request;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AtualizarCoordenadasPosicaoRequest(
        @NotNull(message = "A coordenada X é obrigatória")
        BigDecimal coordX,
        @NotNull(message = "A coordenada Y é obrigatória")
        BigDecimal coordY
) {
}
