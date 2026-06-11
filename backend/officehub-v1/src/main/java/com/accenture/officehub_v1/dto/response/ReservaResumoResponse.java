package com.accenture.officehub_v1.dto.response;

import com.accenture.officehub_v1.entity.Reserva;
import com.accenture.officehub_v1.entity.enums.StatusReserva;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ReservaResumoResponse(
        UUID id,
        UUID salaId,
        String salaNome,
        UUID solicitanteId,
        String solicitanteNome,
        LocalDate dataReserva,
        LocalTime horaInicio,
        LocalTime horaFim,
        Integer quantidadePessoas,
        StatusReserva status,
        String motivoCancelamento,
        String canceladoPorNome,
        OffsetDateTime canceladoEm
) {

    public static ReservaResumoResponse from(Reserva reserva) {
        return new ReservaResumoResponse(
                reserva.getId(),
                reserva.getSala().getId(),
                reserva.getSala().getNome(),
                reserva.getSolicitante().getId(),
                reserva.getSolicitante().getNome(),
                reserva.getDataReserva(),
                reserva.getHoraInicio(),
                reserva.getHoraFim(),
                reserva.getQuantidadePessoas(),
                reserva.getStatus(),
                reserva.getMotivoCancelamento(),
                reserva.getCanceladoPor() != null ? reserva.getCanceladoPor().getNome() : null,
                reserva.getCanceladoEm());
    }
}
