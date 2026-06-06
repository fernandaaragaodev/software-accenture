package com.accenture.officehub_v1.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record CriarEquipeRequest(
        @NotBlank(message = "O nome da equipe é obrigatório") String nome,
        String descricao,
        UUID gestorId,
        @NotEmpty(message = "A equipe deve ter pelo menos um membro") List<UUID> membrosIds
) {
}
