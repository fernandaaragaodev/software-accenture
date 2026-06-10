package com.accenture.officehub_v1.dto.ia;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record PosicaoLivreEntradaDto(
        @JsonProperty("id") UUID id,
        @JsonProperty("coordX") BigDecimal coordX,
        @JsonProperty("coordY") BigDecimal coordY,
        @JsonProperty("tipo") String tipo,
        @JsonProperty("equipamentos") List<String> equipamentos
) {
    public PosicaoLivreEntradaDto(UUID id, BigDecimal coordX, BigDecimal coordY, String tipo) {
        this(id, coordX, coordY, tipo, List.of());
    }
}
