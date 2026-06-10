package com.accenture.officehub_v1.service.ia.motor;

import com.accenture.officehub_v1.dto.ia.AlocacaoAgenteEntradaDto;
import com.accenture.officehub_v1.dto.ia.AlocacaoAgenteSaidaDto;
import com.accenture.officehub_v1.dto.ia.PessoaAlocacaoEntradaDto;
import com.accenture.officehub_v1.dto.ia.PosicaoLivreEntradaDto;
import com.accenture.officehub_v1.entity.enums.TipoPessoaAlocacao;
import com.accenture.officehub_v1.service.CriterioProximidade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MotorAlocacaoEspacialTest {

    private MotorAlocacaoEspacial motor;

    private static final UUID SALA_ID = UUID.fromString("10000000-0000-0000-0000-000000000100");
    private static final UUID EQUIPE_ID = UUID.fromString("20000000-0000-0000-0000-000000000200");

    @BeforeEach
    void setUp() {
        motor = new MotorAlocacaoEspacial();
    }

    @Test
    void deveCalcularScorePositivoParaFuncionarioProximoDaEquipe() {
        UUID func1 = UUID.randomUUID();
        UUID func2 = UUID.randomUUID();

        PessoaAlocacaoEntradaDto pessoa1 = pessoa(func1, "Ana", TipoPessoaAlocacao.FUNCIONARIO, EQUIPE_ID);
        PessoaAlocacaoEntradaDto pessoa2 = pessoa(func2, "Bia", TipoPessoaAlocacao.FUNCIONARIO, EQUIPE_ID);

        PosicaoLivreEntradaDto pos1 = posicao(UUID.randomUUID(), 0, 0, "PADRAO");
        PosicaoLivreEntradaDto pos2 = posicao(UUID.randomUUID(), 1, 0, "PADRAO");

        AlocacaoAgenteEntradaDto entrada = entrada(
                List.of(pessoa1, pessoa2),
                List.of(pos1, pos2),
                CriterioProximidade.PREFERENCIAL);

        int score = motor.calcularScore(entrada, pessoa2, pos2, java.util.Map.of(func1, pos1));

        assertThat(score).isGreaterThanOrEqualTo(MotorAlocacaoEspacial.SCORE_MESMO_TIME_PROXIMO);
    }

    @Test
    void deveEscolherMelhorPosicaoParaVisitanteProximoDaEntrada() {
        UUID visitanteId = UUID.randomUUID();
        PessoaAlocacaoEntradaDto visitante = pessoa(visitanteId, "Visitante", TipoPessoaAlocacao.VISITANTE, null);

        PosicaoLivreEntradaDto proxima = posicao(UUID.randomUUID(), 0, 0, "PADRAO");
        PosicaoLivreEntradaDto distante = posicao(UUID.randomUUID(), 20, 20, "PADRAO");

        AlocacaoAgenteEntradaDto entrada = entrada(
                List.of(visitante),
                List.of(proxima, distante),
                CriterioProximidade.PREFERENCIAL);

        Optional<PosicaoLivreEntradaDto> melhor = motor.buscarMelhorPosicao(
                entrada,
                visitante,
                java.util.Map.of(proxima.id(), proxima, distante.id(), distante),
                java.util.Map.of(),
                java.util.Set.of());

        assertThat(melhor).contains(proxima);
    }

    @Test
    void deveEscolherMelhorPosicaoParaGestorProximoDaEquipe() {
        UUID gestorId = UUID.randomUUID();
        UUID funcionarioId = UUID.randomUUID();

        PessoaAlocacaoEntradaDto gestor = pessoa(gestorId, "Gestor", TipoPessoaAlocacao.GESTOR, EQUIPE_ID);
        PessoaAlocacaoEntradaDto funcionario = pessoa(funcionarioId, "Membro", TipoPessoaAlocacao.FUNCIONARIO, EQUIPE_ID);

        PosicaoLivreEntradaDto posMembro = posicao(UUID.randomUUID(), 0, 0, "PADRAO");
        PosicaoLivreEntradaDto posGestorProxima = posicao(UUID.randomUUID(), 1, 0, "PADRAO");
        PosicaoLivreEntradaDto posGestorDistante = posicao(UUID.randomUUID(), 30, 30, "PADRAO");

        AlocacaoAgenteEntradaDto entrada = entrada(
                List.of(gestor, funcionario),
                List.of(posMembro, posGestorProxima, posGestorDistante),
                CriterioProximidade.PREFERENCIAL);

        Optional<PosicaoLivreEntradaDto> melhor = motor.buscarMelhorPosicao(
                entrada,
                gestor,
                java.util.Map.of(
                        posMembro.id(), posMembro,
                        posGestorProxima.id(), posGestorProxima,
                        posGestorDistante.id(), posGestorDistante),
                java.util.Map.of(funcionarioId, posMembro),
                java.util.Set.of(posMembro.id()));

        assertThat(melhor).contains(posGestorProxima);
    }

    @Test
    void deveValidarRestricoesQuandoFaltamPosicoes() {
        UUID pessoaId = UUID.randomUUID();
        AlocacaoAgenteEntradaDto entrada = entrada(
                List.of(pessoa(pessoaId, "João", TipoPessoaAlocacao.FUNCIONARIO, EQUIPE_ID),
                        pessoa(UUID.randomUUID(), "Maria", TipoPessoaAlocacao.FUNCIONARIO, EQUIPE_ID)),
                List.of(posicao(UUID.randomUUID(), 0, 0, "PADRAO")),
                CriterioProximidade.PREFERENCIAL);

        Optional<String> erro = motor.validarRestricoes(entrada);

        assertThat(erro).isPresent();
        assertThat(erro.get()).contains("posições livres suficientes");
    }

    @Test
    void deveAlocarEquipeComAgrupamento() {
        UUID func1 = UUID.randomUUID();
        UUID func2 = UUID.randomUUID();

        AlocacaoAgenteEntradaDto entrada = entrada(
                List.of(
                        pessoa(func1, "Ana", TipoPessoaAlocacao.FUNCIONARIO, EQUIPE_ID),
                        pessoa(func2, "Bia", TipoPessoaAlocacao.FUNCIONARIO, EQUIPE_ID)),
                List.of(
                        posicao(UUID.randomUUID(), 0, 0, "PADRAO"),
                        posicao(UUID.randomUUID(), 1, 0, "PADRAO")),
                CriterioProximidade.PREFERENCIAL);

        AlocacaoAgenteSaidaDto saida = motor.executar(entrada);

        assertThat(saida.sucesso()).isTrue();
        assertThat(saida.alocacoes()).hasSize(2);
        assertThat(saida.scoreTotal()).isGreaterThan(0);
    }

    @Test
    void deveFalharQuandoNaoExisteSolucaoValida() {
        UUID pessoa1 = UUID.randomUUID();
        UUID pessoa2 = UUID.randomUUID();

        AlocacaoAgenteEntradaDto entrada = entrada(
                List.of(
                        pessoa(pessoa1, "Ana", TipoPessoaAlocacao.FUNCIONARIO, EQUIPE_ID),
                        pessoa(pessoa2, "Bia", TipoPessoaAlocacao.FUNCIONARIO, EQUIPE_ID)),
                List.of(
                        posicao(UUID.randomUUID(), 0, 0, "PADRAO"),
                        posicao(UUID.randomUUID(), 20, 0, "PADRAO")),
                CriterioProximidade.OBRIGATORIA);

        AlocacaoAgenteSaidaDto saida = motor.executar(entrada);

        assertThat(saida.sucesso()).isFalse();
        assertThat(saida.motivoFalha()).isNotBlank();
    }

    @Test
    void deveAlocarPorEquipamentoVinculadoQuandoTipoDaPosicaoDiferente() {
        UUID pessoaId = UUID.randomUUID();
        PessoaAlocacaoEntradaDto pessoa = new PessoaAlocacaoEntradaDto(
                pessoaId,
                "Administrador",
                TipoPessoaAlocacao.FUNCIONARIO,
                null,
                List.of("Monitor menor", "Mesa digitalizadora", "Monitor"));

        PosicaoLivreEntradaDto posComEquipamento = new PosicaoLivreEntradaDto(
                UUID.randomUUID(),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                "INDIVIDUAL",
                List.of("Monitor menor"));

        AlocacaoAgenteEntradaDto entrada = entrada(
                List.of(pessoa),
                List.of(posComEquipamento),
                CriterioProximidade.PREFERENCIAL);

        AlocacaoAgenteSaidaDto saida = motor.executar(entrada);

        assertThat(saida.sucesso()).isTrue();
        assertThat(saida.alocacoes()).hasSize(1);
        assertThat(saida.alocacoes().get(0).posicaoId()).isEqualTo(posComEquipamento.id());
    }

    @Test
    void deveAplicarPenalidadePorQuebraDePreferencia() {
        UUID pessoaId = UUID.randomUUID();
        PessoaAlocacaoEntradaDto pessoa = new PessoaAlocacaoEntradaDto(
                pessoaId,
                "João",
                TipoPessoaAlocacao.FUNCIONARIO,
                EQUIPE_ID,
                List.of("EXECUTIVA"));

        PosicaoLivreEntradaDto posIncompativel = posicao(UUID.randomUUID(), 0, 0, "PADRAO");
        AlocacaoAgenteEntradaDto entrada = entrada(List.of(pessoa), List.of(posIncompativel), CriterioProximidade.PREFERENCIAL);

        Optional<PosicaoLivreEntradaDto> melhor = motor.buscarMelhorPosicao(
                entrada,
                pessoa,
                java.util.Map.of(posIncompativel.id(), posIncompativel),
                java.util.Map.of(),
                java.util.Set.of());

        assertThat(melhor).isEmpty();
    }

    private AlocacaoAgenteEntradaDto entrada(
            List<PessoaAlocacaoEntradaDto> pessoas,
            List<PosicaoLivreEntradaDto> posicoes,
            String criterioProximidade) {

        return new AlocacaoAgenteEntradaDto(
                SALA_ID,
                LocalDate.of(2026, 6, 10),
                "EQUIPE",
                criterioProximidade,
                BigDecimal.valueOf(5),
                10,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                pessoas,
                posicoes);
    }

    private PessoaAlocacaoEntradaDto pessoa(
            UUID id,
            String nome,
            TipoPessoaAlocacao tipo,
            UUID equipeId) {
        return new PessoaAlocacaoEntradaDto(id, nome, tipo, equipeId, List.of("PADRAO"));
    }

    private PosicaoLivreEntradaDto posicao(UUID id, double x, double y, String tipo) {
        return new PosicaoLivreEntradaDto(id, BigDecimal.valueOf(x), BigDecimal.valueOf(y), tipo);
    }
}
