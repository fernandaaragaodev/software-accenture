package com.accenture.officehub_v1.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CriarSalaRequest(
        @NotBlank(message = "O nome da sala é obrigatório")
        String nome,
        String descricao,
        Integer andar,
        String bloco,
        @NotNull(message = "A capacidade máxima é obrigatória")
        @Min(value = 1, message = "A capacidade máxima deve ser maior que zero")
        Integer capacidadeMaxima,
        BigDecimal raioProximidade,
        String imagemPath
) {
}
