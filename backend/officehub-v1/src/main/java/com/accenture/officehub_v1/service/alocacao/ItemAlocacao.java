package com.accenture.officehub_v1.service.alocacao;

import com.accenture.officehub_v1.dto.request.PessoaReservaRequest;
import com.accenture.officehub_v1.entity.Posicao;

public record ItemAlocacao(
        PessoaReservaRequest pessoa,
        Posicao posicao
) {
}
