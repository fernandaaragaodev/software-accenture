package com.accenture.officehub_v1.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CriarLayoutRequest(
        @NotNull(message = "O ID da sala é obrigatório")
        UUID salaId,
        String versao
) {
}
