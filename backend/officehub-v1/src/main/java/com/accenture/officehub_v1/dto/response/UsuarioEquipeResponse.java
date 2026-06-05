package com.accenture.officehub_v1.dto.response;

import com.accenture.officehub_v1.entity.Usuario;

import java.util.UUID;

public record UsuarioEquipeResponse(
        UUID id,
        String nome,
        String email
) {
    public static UsuarioEquipeResponse from(Usuario usuario) {
        return new UsuarioEquipeResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail());
    }
}
