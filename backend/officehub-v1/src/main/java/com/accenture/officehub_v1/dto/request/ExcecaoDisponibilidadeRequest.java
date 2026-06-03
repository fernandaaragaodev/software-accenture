package com.accenture.officehub_v1.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ExcecaoDisponibilidadeRequest(
        @NotNull(message = "A data da exceção é obrigatória")
        LocalDate data,
        String motivo
) {
}
