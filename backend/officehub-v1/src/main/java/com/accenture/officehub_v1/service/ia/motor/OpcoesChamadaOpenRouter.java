package com.accenture.officehub_v1.service.ia.motor;

public record OpcoesChamadaOpenRouter(
        double temperatura,
        int tentativa,
        boolean novaSugestao,
        boolean repeticaoPermitida,
        boolean ignorarCombinacoesExcluidas
) {
    public static OpcoesChamadaOpenRouter padrao() {
        return new OpcoesChamadaOpenRouter(0.1, 0, false, false, false);
    }
}
