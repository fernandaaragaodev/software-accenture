package com.accenture.officehub_v1.dto.response;

import com.accenture.officehub_v1.entity.Posicao;

import java.util.UUID;

public record PosicaoOcupadaResponse(
        UUID id,
        String identificador,
        String tipo
) {
    public static PosicaoOcupadaResponse from(Posicao posicao) {
        return new PosicaoOcupadaResponse(
                posicao.getId(),
                posicao.getIdentificador(),
                posicao.getTipo());
    }
}
