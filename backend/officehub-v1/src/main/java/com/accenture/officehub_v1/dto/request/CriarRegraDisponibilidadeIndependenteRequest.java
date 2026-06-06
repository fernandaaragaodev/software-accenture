package com.accenture.officehub_v1.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CriarRegraDisponibilidadeIndependenteRequest(
        @NotBlank(message = "O nome da regra é obrigatório")
        String nome,
        @NotNull(message = "A antecedência mínima em dias é obrigatória")
        @Min(value = 0, message = "A antecedência mínima não pode ser negativa")
        Integer antecedenciaMinimaDias,
        @NotEmpty(message = "Informe ao menos um horário de disponibilidade")
        @Valid
        List<HorarioDisponibilidadeRequest> horarios
) {
}
