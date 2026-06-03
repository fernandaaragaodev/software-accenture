package com.accenture.officehub_v1.dto.response;

import java.time.LocalDate;
import java.util.UUID;

public record ValidacaoDisponibilidadeResponse(
        UUID salaId,
        LocalDate data,
        boolean disponivel,
        String mensagem
) {
}
