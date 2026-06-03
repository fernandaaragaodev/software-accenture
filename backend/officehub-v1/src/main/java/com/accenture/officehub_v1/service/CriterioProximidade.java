package com.accenture.officehub_v1.service;

public final class CriterioProximidade {

    public static final String OBRIGATORIA = "OBRIGATORIA";
    public static final String PREFERENCIAL = "PREFERENCIAL";

    private CriterioProximidade() {
    }

    public static boolean isObrigatoria(String criterio) {
        return OBRIGATORIA.equalsIgnoreCase(criterio != null ? criterio.trim() : "");
    }

    public static boolean isPreferencial(String criterio) {
        return PREFERENCIAL.equalsIgnoreCase(criterio != null ? criterio.trim() : "");
    }
}
