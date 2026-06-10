package com.accenture.officehub_v1.service.ia;

import java.util.List;
import java.util.Objects;

public final class PosicaoCompatibilidade {

    private PosicaoCompatibilidade() {
    }

    public static boolean compativel(String tipoPosicao, List<String> equipamentos, List<String> preferencias) {
        if (preferencias == null || preferencias.isEmpty()) {
            return true;
        }

        List<String> prefsNormalizadas = preferencias.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        if (prefsNormalizadas.isEmpty()) {
            return true;
        }

        if (tipoPosicao != null && !tipoPosicao.isBlank()) {
            String tipo = tipoPosicao.trim();
            if (prefsNormalizadas.stream().anyMatch(pref -> pref.equalsIgnoreCase(tipo))) {
                return true;
            }
        }

        if (equipamentos == null || equipamentos.isEmpty()) {
            return false;
        }

        return equipamentos.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .anyMatch(eq -> prefsNormalizadas.stream().anyMatch(pref -> pref.equalsIgnoreCase(eq)));
    }
}
