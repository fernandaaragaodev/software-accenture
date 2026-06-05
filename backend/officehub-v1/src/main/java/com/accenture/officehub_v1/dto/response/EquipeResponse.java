package com.accenture.officehub_v1.dto.response;

import com.accenture.officehub_v1.entity.Equipe;
import com.accenture.officehub_v1.entity.EquipeGestor;
import com.accenture.officehub_v1.entity.EquipeMembro;

import java.util.List;
import java.util.UUID;

public record EquipeResponse(
        UUID id,
        String nome,
        String descricao,
        List<UsuarioEquipeResponse> gestores,
        List<UsuarioEquipeResponse> membros
) {
    public static EquipeResponse from(Equipe equipe) {
        List<UsuarioEquipeResponse> gestores = equipe.getGestores().stream()
                .map(EquipeGestor::getUsuario)
                .map(UsuarioEquipeResponse::from)
                .toList();

        List<UsuarioEquipeResponse> membros = equipe.getMembros().stream()
                .map(EquipeMembro::getUsuario)
                .map(UsuarioEquipeResponse::from)
                .toList();

        return new EquipeResponse(
                equipe.getId(),
                equipe.getNome(),
                equipe.getDescricao(),
                gestores,
                membros);
    }
}
