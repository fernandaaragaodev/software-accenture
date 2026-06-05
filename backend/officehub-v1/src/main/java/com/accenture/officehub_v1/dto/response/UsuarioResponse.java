package com.accenture.officehub_v1.dto.response;

import com.accenture.officehub_v1.entity.Usuario;

import java.util.List;
import java.util.UUID;

public record UsuarioResponse(
        UUID id,
        String nome,
        String email,
        boolean ativo,
        UUID gestorId,
        List<String> perfis
) {
    public static UsuarioResponse from(Usuario usuario, List<String> perfis) {
        UUID gestorId = usuario.getGestor() != null ? usuario.getGestor().getId() : null;
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                Boolean.TRUE.equals(usuario.getAtivo()),
                gestorId,
                perfis);
    }
}
