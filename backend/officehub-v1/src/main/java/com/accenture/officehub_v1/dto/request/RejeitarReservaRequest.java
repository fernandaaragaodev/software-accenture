package com.accenture.officehub_v1.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RejeitarReservaRequest(
        @NotBlank(message = "O motivo da rejeição é obrigatório")
        String motivo
) {
}
