package com.accenture.officehub_v1.service.alocacao;

import java.util.List;

public record ResultadoAlocacao(
        boolean sucesso,
        String motivoFalha,
        List<ItemAlocacao> alocacoes,
        String avisoProximidade
) {

    public static ResultadoAlocacao sucesso(List<ItemAlocacao> alocacoes) {
        return sucesso(alocacoes, null);
    }

    public static ResultadoAlocacao sucesso(List<ItemAlocacao> alocacoes, String avisoProximidade) {
        return new ResultadoAlocacao(true, null, alocacoes, avisoProximidade);
    }

    public static ResultadoAlocacao falha(String motivo) {
        return new ResultadoAlocacao(false, motivo, List.of(), null);
    }
}
