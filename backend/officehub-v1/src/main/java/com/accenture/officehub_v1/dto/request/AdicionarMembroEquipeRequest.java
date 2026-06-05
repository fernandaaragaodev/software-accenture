package com.accenture.officehub_v1.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AdicionarMembroEquipeRequest(
        @NotNull(message = "O identificador do funcionário é obrigatório") UUID usuarioId
) {
}
