package com.accenture.officehub_v1.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TipoEquipamentoRequest(
        @NotBlank(message = "O nome do tipo de equipamento é obrigatório")
        String nome,
        String descricao,
        Boolean ativo
) {
}
