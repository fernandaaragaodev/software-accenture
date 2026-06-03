package com.accenture.officehub_v1.dto.response;

import com.accenture.officehub_v1.entity.Posicao;

import java.math.BigDecimal;
import java.util.UUID;

public record PosicaoResponse(
        UUID id,
        UUID salaId,
        UUID layoutId,
        String identificador,
        String tipo,
        BigDecimal coordX,
        BigDecimal coordY,
        String tipoCadeira,
        String tipoMesa,
        String status,
        Boolean ajustadoManualmente
) {

    public static PosicaoResponse from(Posicao posicao) {
        return new PosicaoResponse(
                posicao.getId(),
                posicao.getSala().getId(),
                posicao.getLayout().getId(),
                posicao.getIdentificador(),
                posicao.getTipo(),
                posicao.getCoordX(),
                posicao.getCoordY(),
                posicao.getTipoCadeira(),
                posicao.getTipoMesa(),
                posicao.getStatus(),
                posicao.getAjustadoManualmente()
        );
    }
}
