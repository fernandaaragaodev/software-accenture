package com.accenture.officehub_v1.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AtribuirRegraSalaRequest(
        @NotNull(message = "O ID da regra é obrigatório")
        UUID regraId
) {
}
