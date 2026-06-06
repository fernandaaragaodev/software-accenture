package com.accenture.officehub_v1.dto.response;

import com.accenture.officehub_v1.entity.Usuario;

import java.util.UUID;

public record UsuarioResumoResponse(
        UUID id,
        String nome,
        String email
) {

    public static UsuarioResumoResponse from(Usuario usuario) {
        return new UsuarioResumoResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail());
    }
}
