package com.accenture.officehub_v1.dto.response;

import com.accenture.officehub_v1.entity.Usuario;
import com.accenture.officehub_v1.service.UsuarioMapperHelper;

import java.util.List;
import java.util.UUID;

public record UsuarioEquipeResponse(
        UUID id,
        String nome,
        String email,
        String cargoNome,
        List<EspecialidadeResponse> especialidades
) {
    public static UsuarioEquipeResponse from(Usuario usuario) {
        return new UsuarioEquipeResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                UsuarioMapperHelper.mapCargoNome(usuario),
                UsuarioMapperHelper.mapEspecialidades(usuario));
    }
}
