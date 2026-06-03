package com.accenture.officehub_v1.service.alocacao;

import java.util.List;

public record ResultadoAlocacao(
        boolean sucesso,
        String motivoFalha,
        List<ItemAlocacao> alocacoes
) {

    public static ResultadoAlocacao sucesso(List<ItemAlocacao> alocacoes) {
        return new ResultadoAlocacao(true, null, alocacoes);
    }

    public static ResultadoAlocacao falha(String motivo) {
        return new ResultadoAlocacao(false, motivo, List.of());
    }
}
