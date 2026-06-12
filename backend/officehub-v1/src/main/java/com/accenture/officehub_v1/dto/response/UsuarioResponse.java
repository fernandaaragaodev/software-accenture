package com.accenture.officehub_v1.dto.response;

import com.accenture.officehub_v1.entity.Usuario;
import com.accenture.officehub_v1.service.UsuarioMapperHelper;

import java.util.List;
import java.util.UUID;

public record UsuarioResponse(
        UUID id,
        String nome,
        String email,
        boolean ativo,
        List<String> perfis,
        CargoResponse cargo,
        List<EspecialidadeResponse> especialidades
) {
    public static UsuarioResponse from(Usuario usuario, List<String> perfis) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                Boolean.TRUE.equals(usuario.getAtivo()),
                perfis,
                UsuarioMapperHelper.mapCargo(usuario),
                UsuarioMapperHelper.mapEspecialidades(usuario));
    }
}
