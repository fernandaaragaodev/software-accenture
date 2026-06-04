package com.accenture.officehub_v1.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static UsuarioAutenticado getUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UsuarioAutenticado usuario)) {
            throw new IllegalStateException("Usuário não autenticado.");
        }
        return usuario;
    }

    public static UUID getUsuarioIdAtual() {
        return getUsuarioAutenticado().getUsuarioId();
    }

    public static String getEmailAtual() {
        return getUsuarioAutenticado().getUsername();
    }
}
