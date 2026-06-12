package com.accenture.officehub_v1.service;

import com.accenture.officehub_v1.entity.Posicao;

public final class PosicaoStatus {

    public static final String ATIVA = "ATIVA";
    public static final String INATIVA = "INATIVA";

    /** Situação da posição em uma data específica (consulta de disponibilidade). */
    public static final String LIVRE = "LIVRE";
    public static final String OCUPADA = "OCUPADA";

    private PosicaoStatus() {
    }

    public static boolean isAtiva(Posicao posicao) {
        return posicao != null && isAtiva(posicao.getStatus());
    }

    public static boolean isAtiva(String status) {
        return status != null && ATIVA.equalsIgnoreCase(status.trim());
    }
}
