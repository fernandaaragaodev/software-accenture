package com.accenture.officehub_v1.dto.response;

import com.accenture.officehub_v1.entity.Reserva;
import com.accenture.officehub_v1.entity.enums.StatusReserva;
import com.accenture.officehub_v1.service.alocacao.ItemAlocacao;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ReservaResponse(
        UUID id,
        UUID salaId,
        UUID solicitanteId,
        LocalDate dataReserva,
        Integer quantidadePessoas,
        String criterioProximidade,
        StatusReserva status,
        String motivoRejeicao,
        List<ReservaPosicaoAlocadaResponse> alocacoes
) {

    public static ReservaResponse from(Reserva reserva) {
        return from(reserva, List.of());
    }

    public static ReservaResponse from(Reserva reserva, List<ItemAlocacao> alocacoes) {
        List<ReservaPosicaoAlocadaResponse> alocacoesResponse = alocacoes.stream()
                .map(ReservaPosicaoAlocadaResponse::from)
                .toList();

        return new ReservaResponse(
                reserva.getId(),
                reserva.getSala().getId(),
                reserva.getSolicitante().getId(),
                reserva.getDataReserva(),
                reserva.getQuantidadePessoas(),
                reserva.getCriterioProximidade(),
                reserva.getStatus(),
                reserva.getMotivoRejeicao(),
                alocacoesResponse
        );
    }
}
