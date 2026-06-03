package com.accenture.officehub_v1.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CancelarReservaRequest(
        @NotBlank(message = "O motivo do cancelamento é obrigatório")
        String motivo
) {
}
