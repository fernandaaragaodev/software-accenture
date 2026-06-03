package com.accenture.officehub_v1.dto.response;

import com.accenture.officehub_v1.entity.Reserva;
import com.accenture.officehub_v1.entity.enums.StatusReserva;

import java.time.LocalDate;
import java.util.UUID;

public record ReservaResponse(
        UUID id,
        UUID salaId,
        UUID solicitanteId,
        LocalDate dataReserva,
        Integer quantidadePessoas,
        String criterioProximidade,
        StatusReserva status,
        String motivoRejeicao
) {

    public static ReservaResponse from(Reserva reserva) {
        return new ReservaResponse(
                reserva.getId(),
                reserva.getSala().getId(),
                reserva.getSolicitante().getId(),
                reserva.getDataReserva(),
                reserva.getQuantidadePessoas(),
                reserva.getCriterioProximidade(),
                reserva.getStatus(),
                reserva.getMotivoRejeicao()
        );
    }
}
