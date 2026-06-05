package com.accenture.officehub_v1.dto.response;

import com.accenture.officehub_v1.entity.Equipe;

import java.util.UUID;

public record EquipeResumoResponse(
        UUID id,
        String nome,
        String descricao,
        int quantidadeGestores,
        int quantidadeMembros
) {
    public static EquipeResumoResponse from(Equipe equipe) {
        return new EquipeResumoResponse(
                equipe.getId(),
                equipe.getNome(),
                equipe.getDescricao(),
                equipe.getGestores().size(),
                equipe.getMembros().size());
    }
}
