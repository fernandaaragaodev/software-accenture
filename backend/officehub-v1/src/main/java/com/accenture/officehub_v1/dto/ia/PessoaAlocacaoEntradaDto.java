package com.accenture.officehub_v1.dto.ia;

import com.accenture.officehub_v1.entity.enums.TipoPessoaAlocacao;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

public record PessoaAlocacaoEntradaDto(
        @JsonProperty("id") UUID id,
        @JsonProperty("nome") String nome,
        @JsonProperty("tipo") TipoPessoaAlocacao tipo,
        @JsonProperty("equipeId") UUID equipeId,
        @JsonProperty("tiposPosicaoCompativeis") List<String> tiposPosicaoCompativeis,
        @JsonProperty("equipamentosPreferidos") List<String> equipamentosPreferidos
) {
    public PessoaAlocacaoEntradaDto(
            UUID id,
            String nome,
            TipoPessoaAlocacao tipo,
            UUID equipeId,
            List<String> preferencias) {
        this(id, nome, tipo, equipeId, preferencias, preferencias);
    }
}
