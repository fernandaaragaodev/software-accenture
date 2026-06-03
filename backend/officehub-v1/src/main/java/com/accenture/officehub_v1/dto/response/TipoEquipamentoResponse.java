package com.accenture.officehub_v1.dto.response;

import com.accenture.officehub_v1.entity.TipoEquipamento;

import java.util.UUID;

public record TipoEquipamentoResponse(
        UUID id,
        String nome,
        String descricao,
        Boolean ativo
) {

    public static TipoEquipamentoResponse from(TipoEquipamento tipoEquipamento) {
        return new TipoEquipamentoResponse(
                tipoEquipamento.getId(),
                tipoEquipamento.getNome(),
                tipoEquipamento.getDescricao(),
                tipoEquipamento.getAtivo()
        );
    }
}
