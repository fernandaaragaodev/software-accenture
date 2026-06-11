package com.accenture.officehub_v1.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AceitarSugestaoReservaRequest(
        @NotNull(message = "O ID da execução da sugestão é obrigatório")
        UUID execucaoId,
        @NotNull @Valid SolicitarReservaRequest reserva
) {
}
