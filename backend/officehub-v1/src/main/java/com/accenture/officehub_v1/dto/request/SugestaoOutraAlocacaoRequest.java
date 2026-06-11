package com.accenture.officehub_v1.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record SugestaoOutraAlocacaoRequest(
        @NotNull @Valid SolicitarReservaRequest reserva,
        List<List<UUID>> combinacoesExcluidas
) {
    public SugestaoOutraAlocacaoRequest {
        if (combinacoesExcluidas == null) {
            combinacoesExcluidas = List.of();
        }
    }
}
