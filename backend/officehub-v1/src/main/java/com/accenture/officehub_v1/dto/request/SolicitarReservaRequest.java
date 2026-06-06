package com.accenture.officehub_v1.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record SolicitarReservaRequest(
        @NotNull(message = "O ID da sala é obrigatório")
        UUID salaId,
        UUID equipeId,
        @NotNull(message = "A data da reserva é obrigatória")
        LocalDate dataReserva,
        @NotNull(message = "A hora de início é obrigatória")
        LocalTime horaInicio,
        @NotNull(message = "A hora de fim é obrigatória")
        LocalTime horaFim,
        @NotNull(message = "A quantidade de pessoas é obrigatória")
        @Min(value = 1, message = "A quantidade de pessoas deve ser maior que zero")
        Integer quantidadePessoas,
        @NotBlank(message = "O critério de proximidade é obrigatório")
        String criterioProximidade,
        @NotEmpty(message = "Informe ao menos uma pessoa para a reserva")
        @Valid
        List<PessoaReservaRequest> pessoas
) {
}
