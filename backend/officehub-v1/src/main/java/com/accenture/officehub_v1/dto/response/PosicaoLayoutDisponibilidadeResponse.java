package com.accenture.officehub_v1.dto.response;

import com.accenture.officehub_v1.entity.Posicao;

import java.math.BigDecimal;
import java.util.UUID;

public record PosicaoLayoutDisponibilidadeResponse(
        UUID id,
        String identificador,
        String tipo,
        BigDecimal coordX,
        BigDecimal coordY,
        String situacao
) {
    public static PosicaoLayoutDisponibilidadeResponse from(Posicao posicao, String situacao) {
        return new PosicaoLayoutDisponibilidadeResponse(
                posicao.getId(),
                posicao.getIdentificador(),
                posicao.getTipo(),
                posicao.getCoordX(),
                posicao.getCoordY(),
                situacao);
    }
}
