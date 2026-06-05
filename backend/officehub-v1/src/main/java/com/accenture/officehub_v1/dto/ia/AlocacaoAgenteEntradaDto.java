package com.accenture.officehub_v1.dto.ia;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record AlocacaoAgenteEntradaDto(
        UUID salaId,
        LocalDate dataReserva,
        String criterioProximidade,
        BigDecimal raioProximidade,
        List<PessoaAlocacaoEntradaDto> pessoas,
        List<PosicaoLivreEntradaDto> posicoesLivres
) {
}
