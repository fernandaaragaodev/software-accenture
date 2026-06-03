package com.accenture.officehub_v1.dto.response;

import com.accenture.officehub_v1.entity.HorarioDisponibilidade;
import com.accenture.officehub_v1.entity.RegraDisponibilidade;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record RegraDisponibilidadeResponse(
        UUID id,
        UUID salaId,
        Integer antecedenciaMinimaDias,
        List<HorarioResponse> horarios
) {

    public record HorarioResponse(
            UUID id,
            Integer diaSemana,
            LocalTime horaAbertura,
            LocalTime horaFechamento
    ) {
    }

    public static RegraDisponibilidadeResponse from(RegraDisponibilidade regra, List<HorarioDisponibilidade> horarios) {
        List<HorarioResponse> horariosResponse = horarios.stream()
                .map(h -> new HorarioResponse(
                        h.getId(),
                        h.getDiaSemana(),
                        h.getHoraAbertura(),
                        h.getHoraFechamento()))
                .toList();

        return new RegraDisponibilidadeResponse(
                regra.getId(),
                regra.getSala().getId(),
                regra.getAntecedenciaMinimaDias(),
                horariosResponse
        );
    }
}
