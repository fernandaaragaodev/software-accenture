package com.accenture.officehub_v1.service.ia.motor;

import com.accenture.officehub_v1.dto.ia.AlocacaoAgenteEntradaDto;
import com.accenture.officehub_v1.dto.ia.AlocacaoAgenteSaidaDto;
import com.accenture.officehub_v1.dto.ia.PessoaAlocacaoEntradaDto;
import com.accenture.officehub_v1.dto.ia.PosicaoAlocadaSaidaDto;
import com.accenture.officehub_v1.dto.ia.PosicaoLivreEntradaDto;
import com.accenture.officehub_v1.entity.enums.TipoPessoaAlocacao;
import com.accenture.officehub_v1.service.CriterioProximidade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AlocacaoRespostaValidadorTest {

    private AlocacaoRespostaValidador validador;

    private UUID pessoaId;
    private UUID posicaoId;

    @BeforeEach
    void setUp() {
        validador = new AlocacaoRespostaValidador();
        pessoaId = UUID.randomUUID();
        posicaoId = UUID.randomUUID();
    }

    @Test
    void deveValidarRespostaCorreta() {
        var entrada = entrada(List.of(pessoa("João")), List.of(posicao(posicaoId, "PADRAO")));
        var saida = AlocacaoAgenteSaidaDto.sucesso(100, List.of(new PosicaoAlocadaSaidaDto(pessoaId, posicaoId)));

        assertThat(validador.validar(entrada, saida)).isEmpty();
    }

    @Test
    void deveRejeitarPosicaoDuplicada() {
        UUID posicao2 = UUID.randomUUID();
        var entrada = entrada(
                List.of(pessoa("João"), pessoa("Maria", UUID.randomUUID())),
                List.of(posicao(posicaoId, "PADRAO"), posicao(posicao2, "PADRAO")));

        var saida = AlocacaoAgenteSaidaDto.sucesso(
                100,
                List.of(
                        new PosicaoAlocadaSaidaDto(pessoaId, posicaoId),
                        new PosicaoAlocadaSaidaDto(UUID.randomUUID(), posicaoId)));

        assertThat(validador.validar(entrada, saida))
                .isPresent()
                .get()
                .asString()
                .contains("duplicou posição");
    }

    @Test
    void deveValidarAlocacaoPorEquipamentoVinculado() {
        var entrada = entrada(
                List.of(new PessoaAlocacaoEntradaDto(
                        pessoaId,
                        "João",
                        TipoPessoaAlocacao.FUNCIONARIO,
                        UUID.randomUUID(),
                        List.of("Monitor menor"))),
                List.of(new PosicaoLivreEntradaDto(
                        posicaoId,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        "INDIVIDUAL",
                        List.of("Monitor menor"))));

        var saida = AlocacaoAgenteSaidaDto.sucesso(100, List.of(new PosicaoAlocadaSaidaDto(pessoaId, posicaoId)));

        assertThat(validador.validar(entrada, saida)).isEmpty();
    }

    @Test
    void deveRejeitarPosicaoNaoLivre() {
        var entrada = entrada(List.of(pessoa("João")), List.of(posicao(posicaoId, "PADRAO")));
        UUID posicaoInvalida = UUID.randomUUID();

        var saida = AlocacaoAgenteSaidaDto.sucesso(
                100,
                List.of(new PosicaoAlocadaSaidaDto(pessoaId, posicaoInvalida)));

        assertThat(validador.validar(entrada, saida))
                .isPresent()
                .get()
                .asString()
                .contains("indisponível");
    }

    private AlocacaoAgenteEntradaDto entrada(
            List<PessoaAlocacaoEntradaDto> pessoas,
            List<PosicaoLivreEntradaDto> posicoes) {

        return new AlocacaoAgenteEntradaDto(
                UUID.randomUUID(),
                LocalDate.of(2026, 6, 10),
                "EQUIPE",
                CriterioProximidade.PREFERENCIAL,
                BigDecimal.valueOf(5),
                10,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                pessoas,
                posicoes);
    }

    private PessoaAlocacaoEntradaDto pessoa(String nome) {
        return pessoa(nome, pessoaId);
    }

    private PessoaAlocacaoEntradaDto pessoa(String nome, UUID id) {
        return new PessoaAlocacaoEntradaDto(
                id,
                nome,
                TipoPessoaAlocacao.FUNCIONARIO,
                UUID.randomUUID(),
                List.of("PADRAO"));
    }

    private PosicaoLivreEntradaDto posicao(UUID id, String tipo) {
        return new PosicaoLivreEntradaDto(id, BigDecimal.ZERO, BigDecimal.ZERO, tipo);
    }
}
