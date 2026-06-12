package com.accenture.officehub_v1.dto.response;

import com.accenture.officehub_v1.entity.Especialidade;

import java.util.UUID;

public record EspecialidadeResponse(
        UUID id,
        String nome,
        String descricao
) {
    public static EspecialidadeResponse from(Especialidade especialidade) {
        return new EspecialidadeResponse(
                especialidade.getId(),
                especialidade.getNome(),
                especialidade.getDescricao());
    }
}
