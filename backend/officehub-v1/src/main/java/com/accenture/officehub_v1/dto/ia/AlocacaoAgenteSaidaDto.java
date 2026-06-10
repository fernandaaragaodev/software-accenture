package com.accenture.officehub_v1.dto.ia;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AlocacaoAgenteSaidaDto(
        @JsonProperty("sucesso") @JsonAlias("success") boolean sucesso,
        @JsonProperty("scoreTotal") @JsonAlias({"score_total", "score"}) Integer scoreTotal,
        @JsonProperty("motivoFalha") @JsonAlias({"motivo_falha", "failureReason", "error"}) String motivoFalha,
        @JsonProperty("avisoProximidade") @JsonAlias("aviso_proximidade") String avisoProximidade,
        @JsonProperty("alocacoes") @JsonAlias({"allocations", "alocacao"}) List<PosicaoAlocadaSaidaDto> alocacoes,
        @JsonProperty("tokensUtilizados") @JsonAlias("tokens_utilizados") Integer tokensUtilizados
) {

    public static AlocacaoAgenteSaidaDto sucesso(int scoreTotal, List<PosicaoAlocadaSaidaDto> alocacoes) {
        return new AlocacaoAgenteSaidaDto(true, scoreTotal, null, null, alocacoes, null);
    }

    public static AlocacaoAgenteSaidaDto sucesso(
            int scoreTotal,
            List<PosicaoAlocadaSaidaDto> alocacoes,
            String avisoProximidade) {
        return new AlocacaoAgenteSaidaDto(true, scoreTotal, null, avisoProximidade, alocacoes, null);
    }

    public static AlocacaoAgenteSaidaDto falha(String motivo) {
        return new AlocacaoAgenteSaidaDto(false, null, motivo, null, List.of(), null);
    }
}
