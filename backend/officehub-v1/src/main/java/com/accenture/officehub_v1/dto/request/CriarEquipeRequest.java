package com.accenture.officehub_v1.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record CriarEquipeRequest(
        @NotBlank(message = "O nome da equipe é obrigatório") String nome,
        String descricao,
        UUID gestorId
) {
}
