package com.accenture.officehub_v1.service.ia.motor;

import com.accenture.officehub_v1.dto.ia.AlocacaoAgenteEntradaDto;
import com.accenture.officehub_v1.dto.ia.AlocacaoAgenteSaidaDto;
import com.accenture.officehub_v1.dto.ia.PessoaAlocacaoEntradaDto;
import com.accenture.officehub_v1.dto.ia.PosicaoAlocadaSaidaDto;
import com.accenture.officehub_v1.dto.ia.PosicaoLivreEntradaDto;
import com.accenture.officehub_v1.service.ia.PosicaoCompatibilidade;
import com.accenture.officehub_v1.entity.enums.TipoPessoaAlocacao;
import com.accenture.officehub_v1.service.CriterioProximidade;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class MotorAlocacaoEspacial implements MotorAlocacao {

    static final int SCORE_MESMO_TIME_PROXIMO = 50;
    static final int SCORE_GESTOR_PROXIMO_EQUIPE = 30;
    static final int SCORE_VISITANTE_PROXIMO_ENTRADA = 20;
    static final int SCORE_DISTANTE_EQUIPE = -40;
    static final int SCORE_QUEBRA_PREFERENCIA = -20;
    static final int SCORE_POSICAO_ISOLADA = -10;
    static final int SCORE_AGRUPAMENTO_COMPLETO = 100;

    private static final double RAIO_PADRAO = 5.0;
    private static final double FATOR_DISTANCIA_LONGA = 2.0;

    @Override
    public AlocacaoAgenteSaidaDto executar(AlocacaoAgenteEntradaDto entrada) {
        Optional<String> erro = validarRestricoes(entrada);
        if (erro.isPresent()) {
            return AlocacaoAgenteSaidaDto.falha(erro.get());
        }
        return executarAlocacao(entrada);
    }

    Optional<String> validarRestricoes(AlocacaoAgenteEntradaDto entrada) {
        if (entrada.posicoesLivres() == null || entrada.posicoesLivres().isEmpty()) {
            return Optional.of("Não há posições livres na data solicitada.");
        }

        if (entrada.pessoas() == null || entrada.pessoas().isEmpty()) {
            return Optional.of("Nenhuma pessoa informada para alocação.");
        }

        if (entrada.capacidadeMaxima() != null
                && entrada.pessoas().size() > entrada.capacidadeMaxima()) {
            return Optional.of(String.format(
                    "A quantidade de pessoas (%d) excede a capacidade da sala (%d).",
                    entrada.pessoas().size(),
                    entrada.capacidadeMaxima()));
        }

        if (entrada.posicoesLivres().size() < entrada.pessoas().size()) {
            return Optional.of(String.format(
                    "Não há posições livres suficientes na data solicitada. "
                            + "Necessárias: %d, disponíveis: %d.",
                    entrada.pessoas().size(),
                    entrada.posicoesLivres().size()));
        }

        Set<UUID> idsPessoas = new HashSet<>();
        for (PessoaAlocacaoEntradaDto pessoa : entrada.pessoas()) {
            if (pessoa.id() == null) {
                return Optional.of("Todas as pessoas devem possuir identificador.");
            }
            if (!idsPessoas.add(pessoa.id())) {
                return Optional.of("Não é permitido duplicar pessoa na reserva.");
            }
        }

        Set<UUID> idsPosicoes = new HashSet<>();
        for (PosicaoLivreEntradaDto posicao : entrada.posicoesLivres()) {
            if (posicao.id() == null) {
                return Optional.of("Todas as posições livres devem possuir identificador.");
            }
            if (!idsPosicoes.add(posicao.id())) {
                return Optional.of("Lista de posições livres contém duplicidade.");
            }
        }

        return Optional.empty();
    }

    AlocacaoAgenteSaidaDto executarAlocacao(AlocacaoAgenteEntradaDto entrada) {
        List<PessoaAlocacaoEntradaDto> pessoasOrdenadas = ordenarPessoas(entrada.pessoas());
        Map<UUID, PosicaoLivreEntradaDto> posicoesPorId = indexarPosicoes(entrada.posicoesLivres());

        Optional<ResultadoBusca> melhor = buscarMelhorCombinacao(
                entrada,
                pessoasOrdenadas,
                posicoesPorId,
                0,
                new HashMap<>(),
                new HashSet<>(),
                0);

        if (melhor.isEmpty()) {
            return AlocacaoAgenteSaidaDto.falha(
                    "Não foi possível encontrar uma alocação válida para todos os participantes.");
        }

        List<PosicaoAlocadaSaidaDto> alocacoes = melhor.get().alocacoes().stream()
                .map(a -> new PosicaoAlocadaSaidaDto(a.pessoaId(), a.posicaoId()))
                .toList();

        String aviso = montarAvisoProximidade(entrada, melhor.get().alocacoes(), posicoesPorId);

        return AlocacaoAgenteSaidaDto.sucesso(melhor.get().scoreTotal(), alocacoes, aviso);
    }

    private Optional<ResultadoBusca> buscarMelhorCombinacao(
            AlocacaoAgenteEntradaDto entrada,
            List<PessoaAlocacaoEntradaDto> pessoas,
            Map<UUID, PosicaoLivreEntradaDto> posicoesPorId,
            int indice,
            Map<UUID, PosicaoLivreEntradaDto> alocadas,
            Set<UUID> posicoesUsadas,
            int scoreParcial) {

        if (indice == pessoas.size()) {
            if (!validarAlocacaoCompleta(entrada, alocadas)) {
                return Optional.empty();
            }

            if (combinacaoExcluida(entrada.combinacoesExcluidas(), alocadas)) {
                return Optional.empty();
            }

            int bonusAgrupamento = calcularBonusAgrupamentoEquipe(entrada, alocadas);
            return Optional.of(new ResultadoBusca(
                    montarAlocacoesParciais(alocadas),
                    scoreParcial + bonusAgrupamento));
        }

        PessoaAlocacaoEntradaDto pessoa = pessoas.get(indice);
        Optional<PosicaoLivreEntradaDto> melhorPosicao = buscarMelhorPosicao(
                entrada, pessoa, posicoesPorId, alocadas, posicoesUsadas);

        if (melhorPosicao.isEmpty()) {
            return Optional.empty();
        }

        List<CandidatoPosicao> candidatos = listarCandidatosOrdenados(
                entrada, pessoa, posicoesPorId, alocadas, posicoesUsadas);

        Optional<ResultadoBusca> melhorResultado = Optional.empty();

        for (CandidatoPosicao candidato : candidatos) {
            alocadas.put(pessoa.id(), candidato.posicao());
            posicoesUsadas.add(candidato.posicao().id());

            Optional<ResultadoBusca> resultado = buscarMelhorCombinacao(
                    entrada,
                    pessoas,
                    posicoesPorId,
                    indice + 1,
                    alocadas,
                    posicoesUsadas,
                    scoreParcial + candidato.score());

            if (resultado.isPresent()
                    && (melhorResultado.isEmpty()
                    || resultado.get().scoreTotal() > melhorResultado.get().scoreTotal())) {
                melhorResultado = resultado;
            }

            alocadas.remove(pessoa.id());
            posicoesUsadas.remove(candidato.posicao().id());
        }

        return melhorResultado;
    }

    Optional<PosicaoLivreEntradaDto> buscarMelhorPosicao(
            AlocacaoAgenteEntradaDto entrada,
            PessoaAlocacaoEntradaDto pessoa,
            Map<UUID, PosicaoLivreEntradaDto> posicoesPorId,
            Map<UUID, PosicaoLivreEntradaDto> alocadas,
            Set<UUID> posicoesUsadas) {

        return listarCandidatosOrdenados(entrada, pessoa, posicoesPorId, alocadas, posicoesUsadas)
                .stream()
                .findFirst()
                .map(CandidatoPosicao::posicao);
    }

    List<CandidatoPosicao> listarCandidatosOrdenados(
            AlocacaoAgenteEntradaDto entrada,
            PessoaAlocacaoEntradaDto pessoa,
            Map<UUID, PosicaoLivreEntradaDto> posicoesPorId,
            Map<UUID, PosicaoLivreEntradaDto> alocadas,
            Set<UUID> posicoesUsadas) {

        List<CandidatoPosicao> candidatos = new ArrayList<>();

        for (PosicaoLivreEntradaDto posicao : posicoesPorId.values()) {
            if (posicoesUsadas.contains(posicao.id())) {
                continue;
            }

            if (!posicaoCompativel(posicao, pessoa)) {
                continue;
            }

            int score = calcularScore(entrada, pessoa, posicao, alocadas);
            candidatos.add(new CandidatoPosicao(posicao, score));
        }

        candidatos.sort(Comparator
                .comparingInt(CandidatoPosicao::score).reversed()
                .thenComparing(c -> c.posicao().id()));

        return candidatos;
    }

    int calcularScore(
            AlocacaoAgenteEntradaDto entrada,
            PessoaAlocacaoEntradaDto pessoa,
            PosicaoLivreEntradaDto posicao,
            Map<UUID, PosicaoLivreEntradaDto> alocadas) {

        int score = 0;
        double raio = raioProximidade(entrada);

        if (!posicaoCompativel(posicao, pessoa)) {
            score += SCORE_QUEBRA_PREFERENCIA;
        }

        if (pessoa.tipo() == TipoPessoaAlocacao.VISITANTE) {
            double distanciaEntrada = distanciaEntrada(entrada, posicao);
            if (distanciaEntrada <= raio) {
                score += SCORE_VISITANTE_PROXIMO_ENTRADA;
            } else if (distanciaEntrada > raio * FATOR_DISTANCIA_LONGA) {
                score += SCORE_QUEBRA_PREFERENCIA;
            }
        }

        if (pessoa.tipo() == TipoPessoaAlocacao.GESTOR || pessoa.tipo() == TipoPessoaAlocacao.LIDER_EQUIPE) {
            if (proximoDaEquipe(pessoa, posicao, alocadas, entrada.pessoas(), raio)) {
                score += SCORE_GESTOR_PROXIMO_EQUIPE;
            }
        }

        if (pessoa.tipo() == TipoPessoaAlocacao.FUNCIONARIO) {
            double distanciaEquipe = distanciaMinimaEquipe(pessoa, posicao, alocadas, entrada.pessoas());
            if (distanciaEquipe <= raio) {
                score += SCORE_MESMO_TIME_PROXIMO;
            } else if (distanciaEquipe > raio * FATOR_DISTANCIA_LONGA) {
                score += SCORE_DISTANTE_EQUIPE;
            }
        }

        if (posicaoIsolada(posicao, alocadas, raio)) {
            score += SCORE_POSICAO_ISOLADA;
        }

        return score;
    }

    int calcularBonusAgrupamentoEquipe(
            AlocacaoAgenteEntradaDto entrada,
            Map<UUID, PosicaoLivreEntradaDto> alocadas) {

        Map<UUID, List<PessoaAlocacaoEntradaDto>> porEquipe = new HashMap<>();

        for (PessoaAlocacaoEntradaDto pessoa : entrada.pessoas()) {
            if (pessoa.equipeId() == null || pessoa.tipo() != TipoPessoaAlocacao.FUNCIONARIO) {
                continue;
            }
            porEquipe.computeIfAbsent(pessoa.equipeId(), k -> new ArrayList<>()).add(pessoa);
        }

        int bonus = 0;
        double raio = raioProximidade(entrada);

        for (List<PessoaAlocacaoEntradaDto> membros : porEquipe.values()) {
            if (membros.size() < 2) {
                continue;
            }

            boolean agrupados = true;
            for (PessoaAlocacaoEntradaDto membro : membros) {
                PosicaoLivreEntradaDto posicao = alocadas.get(membro.id());
                if (posicao == null) {
                    agrupados = false;
                    break;
                }

                for (PessoaAlocacaoEntradaDto outro : membros) {
                    if (membro.id().equals(outro.id())) {
                        continue;
                    }
                    PosicaoLivreEntradaDto outraPosicao = alocadas.get(outro.id());
                    if (outraPosicao == null || distancia(posicao, outraPosicao) > raio) {
                        agrupados = false;
                        break;
                    }
                }

                if (!agrupados) {
                    break;
                }
            }

            if (agrupados) {
                bonus += SCORE_AGRUPAMENTO_COMPLETO;
            }
        }

        return bonus;
    }

    private boolean validarAlocacaoCompleta(
            AlocacaoAgenteEntradaDto entrada,
            Map<UUID, PosicaoLivreEntradaDto> alocadas) {

        if (alocadas.size() != entrada.pessoas().size()) {
            return false;
        }

        Set<UUID> posicoesUsadas = new HashSet<>();
        for (PosicaoLivreEntradaDto posicao : alocadas.values()) {
            if (!posicoesUsadas.add(posicao.id())) {
                return false;
            }
        }

        if (CriterioProximidade.isObrigatoria(entrada.criterioProximidade())) {
            double raio = raioProximidade(entrada);
            List<PosicaoLivreEntradaDto> posicoes = new ArrayList<>(alocadas.values());
            for (int i = 0; i < posicoes.size(); i++) {
                for (int j = i + 1; j < posicoes.size(); j++) {
                    if (distancia(posicoes.get(i), posicoes.get(j)) > raio) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private String montarAvisoProximidade(
            AlocacaoAgenteEntradaDto entrada,
            List<AlocacaoParcial> alocacoes,
            Map<UUID, PosicaoLivreEntradaDto> posicoesPorId) {

        if (!CriterioProximidade.isPreferencial(entrada.criterioProximidade())
                || alocacoes.size() <= 1) {
            return null;
        }

        double raio = raioProximidade(entrada);
        List<PosicaoLivreEntradaDto> posicoes = alocacoes.stream()
                .map(a -> posicoesPorId.get(a.posicaoId()))
                .toList();

        for (int i = 0; i < posicoes.size(); i++) {
            for (int j = i + 1; j < posicoes.size(); j++) {
                if (distancia(posicoes.get(i), posicoes.get(j)) > raio) {
                    return "As posições foram alocadas, porém nem todas ficaram dentro do raio de proximidade "
                            + "preferencial configurado para a sala.";
                }
            }
        }

        return null;
    }

    private List<PessoaAlocacaoEntradaDto> ordenarPessoas(List<PessoaAlocacaoEntradaDto> pessoas) {
        return pessoas.stream()
                .sorted(Comparator.comparingInt(p -> prioridadeTipo(p.tipo())))
                .toList();
    }

    private int prioridadeTipo(TipoPessoaAlocacao tipo) {
        if (tipo == null) {
            return 99;
        }
        return switch (tipo) {
            case LIDER_EQUIPE -> 1;
            case GESTOR -> 2;
            case FUNCIONARIO -> 3;
            case VISITANTE -> 4;
        };
    }

    private Map<UUID, PosicaoLivreEntradaDto> indexarPosicoes(List<PosicaoLivreEntradaDto> posicoes) {
        Map<UUID, PosicaoLivreEntradaDto> mapa = new HashMap<>();
        for (PosicaoLivreEntradaDto posicao : posicoes) {
            mapa.put(posicao.id(), posicao);
        }
        return mapa;
    }

    private boolean combinacaoExcluida(
            List<List<UUID>> combinacoesExcluidas,
            Map<UUID, PosicaoLivreEntradaDto> alocadas) {
        if (combinacoesExcluidas == null || combinacoesExcluidas.isEmpty()) {
            return false;
        }

        Set<UUID> atual = alocadas.values().stream()
                .map(PosicaoLivreEntradaDto::id)
                .collect(java.util.stream.Collectors.toSet());

        for (List<UUID> excluida : combinacoesExcluidas) {
            if (new HashSet<>(excluida).equals(atual)) {
                return true;
            }
        }

        return false;
    }

    private List<AlocacaoParcial> montarAlocacoesParciais(Map<UUID, PosicaoLivreEntradaDto> alocadas) {
        return alocadas.entrySet().stream()
                .map(e -> new AlocacaoParcial(e.getKey(), e.getValue().id()))
                .toList();
    }

    private boolean posicaoCompativel(PosicaoLivreEntradaDto posicao, PessoaAlocacaoEntradaDto pessoa) {
        return PosicaoCompatibilidade.compativel(
                posicao.tipo(),
                posicao.equipamentos(),
                pessoa.tiposPosicaoCompativeis());
    }

    private boolean proximoDaEquipe(
            PessoaAlocacaoEntradaDto pessoa,
            PosicaoLivreEntradaDto posicao,
            Map<UUID, PosicaoLivreEntradaDto> alocadas,
            List<PessoaAlocacaoEntradaDto> pessoas,
            double raio) {

        for (PessoaAlocacaoEntradaDto outra : pessoas) {
            if (outra.id().equals(pessoa.id())
                    || outra.equipeId() == null
                    || !outra.equipeId().equals(pessoa.equipeId())
                    || outra.tipo() != TipoPessoaAlocacao.FUNCIONARIO) {
                continue;
            }

            PosicaoLivreEntradaDto posicaoOutra = alocadas.get(outra.id());
            if (posicaoOutra != null && distancia(posicao, posicaoOutra) <= raio) {
                return true;
            }
        }

        return false;
    }

    private double distanciaMinimaEquipe(
            PessoaAlocacaoEntradaDto pessoa,
            PosicaoLivreEntradaDto posicao,
            Map<UUID, PosicaoLivreEntradaDto> alocadas,
            List<PessoaAlocacaoEntradaDto> pessoas) {

        double menor = Double.MAX_VALUE;

        for (PessoaAlocacaoEntradaDto outra : pessoas) {
            if (outra.id().equals(pessoa.id())
                    || outra.equipeId() == null
                    || !outra.equipeId().equals(pessoa.equipeId())) {
                continue;
            }

            PosicaoLivreEntradaDto posicaoOutra = alocadas.get(outra.id());
            if (posicaoOutra != null) {
                menor = Math.min(menor, distancia(posicao, posicaoOutra));
            }
        }

        return menor;
    }

    private boolean posicaoIsolada(
            PosicaoLivreEntradaDto posicao,
            Map<UUID, PosicaoLivreEntradaDto> alocadas,
            double raio) {

        if (alocadas.isEmpty()) {
            return false;
        }

        return alocadas.values().stream()
                .noneMatch(outra -> distancia(posicao, outra) <= raio);
    }

    private double distanciaEntrada(AlocacaoAgenteEntradaDto entrada, PosicaoLivreEntradaDto posicao) {
        BigDecimal entradaX = entrada.coordEntradaX() != null ? entrada.coordEntradaX() : BigDecimal.ZERO;
        BigDecimal entradaY = entrada.coordEntradaY() != null ? entrada.coordEntradaY() : BigDecimal.ZERO;

        if (posicao.coordX() == null || posicao.coordY() == null) {
            return Double.MAX_VALUE;
        }

        double dx = posicao.coordX().doubleValue() - entradaX.doubleValue();
        double dy = posicao.coordY().doubleValue() - entradaY.doubleValue();
        return Math.sqrt(dx * dx + dy * dy);
    }

    double distancia(PosicaoLivreEntradaDto a, PosicaoLivreEntradaDto b) {
        if (a.coordX() == null || a.coordY() == null || b.coordX() == null || b.coordY() == null) {
            return Double.POSITIVE_INFINITY;
        }

        double dx = a.coordX().doubleValue() - b.coordX().doubleValue();
        double dy = a.coordY().doubleValue() - b.coordY().doubleValue();
        return Math.sqrt(dx * dx + dy * dy);
    }

    private double raioProximidade(AlocacaoAgenteEntradaDto entrada) {
        if (entrada.raioProximidade() != null) {
            return entrada.raioProximidade().doubleValue();
        }
        return RAIO_PADRAO;
    }

    record CandidatoPosicao(PosicaoLivreEntradaDto posicao, int score) {
    }

    record AlocacaoParcial(UUID pessoaId, UUID posicaoId) {
    }

    record ResultadoBusca(List<AlocacaoParcial> alocacoes, int scoreTotal) {
    }
}
