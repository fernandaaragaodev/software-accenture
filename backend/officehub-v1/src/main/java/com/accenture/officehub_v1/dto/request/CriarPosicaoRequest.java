package com.accenture.officehub_v1.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CriarPosicaoRequest(
        @NotNull(message = "O ID da sala é obrigatório")
        UUID salaId,
        @NotBlank(message = "O identificador da posição é obrigatório")
        String identificador,
        String tipo,
        BigDecimal coordX,
        BigDecimal coordY,
        BigDecimal pixelX,
        BigDecimal pixelY,
        String tipoCadeira,
        String tipoMesa
) {
}
