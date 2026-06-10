package com.accenture.officehub_v1.dto.ia;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record AlocacaoAgenteEntradaDto(
        @JsonProperty("salaId") UUID salaId,
        @JsonProperty("data") LocalDate data,
        @JsonProperty("criterio") String criterio,
        @JsonProperty("criterioProximidade") String criterioProximidade,
        @JsonProperty("raioProximidade") BigDecimal raioProximidade,
        @JsonProperty("capacidadeMaxima") Integer capacidadeMaxima,
        @JsonProperty("coordEntradaX") BigDecimal coordEntradaX,
        @JsonProperty("coordEntradaY") BigDecimal coordEntradaY,
        @JsonProperty("pessoas") List<PessoaAlocacaoEntradaDto> pessoas,
        @JsonProperty("posicoesLivres") List<PosicaoLivreEntradaDto> posicoesLivres
) {
}
