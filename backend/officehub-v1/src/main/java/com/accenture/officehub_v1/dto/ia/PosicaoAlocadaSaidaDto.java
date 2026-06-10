package com.accenture.officehub_v1.dto.ia;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PosicaoAlocadaSaidaDto(
        @JsonProperty("pessoaId") @JsonAlias({"pessoa_id", "personId", "usuarioId", "usuario_id"}) UUID pessoaId,
        @JsonProperty("posicaoId") @JsonAlias({"posicao_id", "positionId"}) UUID posicaoId
) {
}
