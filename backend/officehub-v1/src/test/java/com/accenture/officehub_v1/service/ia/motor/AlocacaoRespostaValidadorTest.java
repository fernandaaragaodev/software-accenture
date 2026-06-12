package com.accenture.officehub_v1.service.ia.motor;

import com.accenture.officehub_v1.dto.ia.AlocacaoAgenteEntradaDto;
import com.accenture.officehub_v1.dto.ia.PessoaAlocacaoEntradaDto;
import com.accenture.officehub_v1.dto.ia.PosicaoLivreEntradaDto;
import com.accenture.officehub_v1.entity.enums.TipoPessoaAlocacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AlocacaoRespostaValidadorTest {

    private AlocacaoRespostaValidador validador;

    @BeforeEach
    void setUp() {
        validador = new AlocacaoRespostaValidador();
    }

    @Test
    void deveDetectarAlternativasEsgotadasParaUmaPessoa() {
        UUID pessoaId = UUID.randomUUID();
        PessoaAlocacaoEntradaDto pessoa = new PessoaAlocacaoEntradaDto(
                pessoaId,
                "João",
                TipoPessoaAlocacao.FUNCIONARIO,
                null,
                List.of("Monitor"));

        PosicaoLivreEntradaDto pos1 = posicao(
                UUID.fromString("10000000-0000-0000-0000-000000000001"),
                List.of("Monitor"));
        PosicaoLivreEntradaDto pos2 = posicao(
                UUID.fromString("10000000-0000-0000-0000-000000000002"),
                List.of("Monitor"));

        AlocacaoAgenteEntradaDto entrada = new AlocacaoAgenteEntradaDto(
                UUID.randomUUID(),
                LocalDate.of(2026, 6, 10),
                "EQUIPE",
                "PREFERENCIAL",
                BigDecimal.valueOf(5),
                10,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                List.of(pessoa),
                List.of(pos1, pos2),
                List.of(List.of(pos1.id()), List.of(pos2.id())));

        assertThat(validador.alternativasEsgotadas(entrada)).isTrue();
    }

    @Test
    void naoDeveMarcarAlternativasEsgotadasQuandoAindaHaPosicoesCompativeis() {
        UUID pessoaId = UUID.randomUUID();
        PessoaAlocacaoEntradaDto pessoa = new PessoaAlocacaoEntradaDto(
                pessoaId,
                "João",
                TipoPessoaAlocacao.FUNCIONARIO,
                null,
                List.of("Monitor"));

        PosicaoLivreEntradaDto pos1 = posicao(
                UUID.fromString("10000000-0000-0000-0000-000000000001"),
                List.of("Monitor"));
        PosicaoLivreEntradaDto pos2 = posicao(
                UUID.fromString("10000000-0000-0000-0000-000000000002"),
                List.of("Monitor"));

        AlocacaoAgenteEntradaDto entrada = new AlocacaoAgenteEntradaDto(
                UUID.randomUUID(),
                LocalDate.of(2026, 6, 10),
                "EQUIPE",
                "PREFERENCIAL",
                BigDecimal.valueOf(5),
                10,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                List.of(pessoa),
                List.of(pos1, pos2),
                List.of(List.of(pos1.id())));

        assertThat(validador.alternativasEsgotadas(entrada)).isFalse();
    }

    private PosicaoLivreEntradaDto posicao(UUID id, List<String> equipamentos) {
        return new PosicaoLivreEntradaDto(
                id,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                "PADRAO",
                equipamentos);
    }
}
