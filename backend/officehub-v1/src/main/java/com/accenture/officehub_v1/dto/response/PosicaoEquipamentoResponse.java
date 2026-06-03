package com.accenture.officehub_v1.dto.response;

import com.accenture.officehub_v1.entity.PosicaoEquipamento;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PosicaoEquipamentoResponse(
        UUID id,
        UUID posicaoId,
        UUID tipoEquipamentoId,
        String tipoEquipamentoNome,
        Integer quantidade,
        String observacao,
        OffsetDateTime createdAt
) {

    public static PosicaoEquipamentoResponse from(PosicaoEquipamento vinculo) {
        return new PosicaoEquipamentoResponse(
                vinculo.getId(),
                vinculo.getPosicao().getId(),
                vinculo.getTipoEquipamento().getId(),
                vinculo.getTipoEquipamento().getNome(),
                vinculo.getQuantidade(),
                vinculo.getObservacao(),
                vinculo.getCreatedAt()
        );
    }
}
