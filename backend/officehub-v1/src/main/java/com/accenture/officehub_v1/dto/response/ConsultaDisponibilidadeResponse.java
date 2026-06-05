package com.accenture.officehub_v1.dto.response;

import com.accenture.officehub_v1.entity.enums.StatusSala;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ConsultaDisponibilidadeResponse(
        UUID salaId,
        LocalDate data,
        StatusSala statusSala,
        boolean disponivelParaReserva,
        String mensagemRegras,
        int totalPosicoes,
        int totalLivres,
        int totalOcupadas,
        int totalInativas,
        Map<String, Long> livresPorTipo,
        List<PosicaoOcupadaResponse> posicoesOcupadas,
        List<PosicaoLayoutDisponibilidadeResponse> layout
) {
}
