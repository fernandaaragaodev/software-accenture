package com.accenture.officehub_v1.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.UUID;

@Getter
public class UsuarioAutenticado extends User {

    private final UUID usuarioId;

    public UsuarioAutenticado(
            UUID usuarioId,
            String email,
            String senhaHash,
            boolean ativo,
            Collection<? extends GrantedAuthority> authorities) {
        super(email, senhaHash, ativo, true, true, true, authorities);
        this.usuarioId = usuarioId;
    }
}
