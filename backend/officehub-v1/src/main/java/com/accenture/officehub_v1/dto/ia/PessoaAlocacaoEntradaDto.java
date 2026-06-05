package com.accenture.officehub_v1.dto.ia;

import java.util.List;
import java.util.UUID;

public record PessoaAlocacaoEntradaDto(
        UUID usuarioId,
        String nomeExterno,
        List<String> tiposPosicaoCompativeis
) {
}
