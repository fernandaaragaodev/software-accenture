package com.accenture.officehub_v1.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record VincularEquipamentoPosicaoRequest(
        @NotNull(message = "O tipo de equipamento é obrigatório")
        UUID tipoEquipamentoId,
        @NotNull(message = "A quantidade é obrigatória")
        @Min(value = 1, message = "A quantidade deve ser maior que zero")
        Integer quantidade,
        String observacao
) {
}
