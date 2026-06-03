package com.accenture.officehub_v1.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record HorarioDisponibilidadeRequest(
        @NotNull(message = "O dia da semana é obrigatório")
        @Min(value = 0, message = "O dia da semana deve estar entre 0 e 6")
        @Max(value = 6, message = "O dia da semana deve estar entre 0 e 6")
        Integer diaSemana,
        @NotNull(message = "A hora de abertura é obrigatória")
        LocalTime horaAbertura,
        @NotNull(message = "A hora de fechamento é obrigatória")
        LocalTime horaFechamento
) {
}
