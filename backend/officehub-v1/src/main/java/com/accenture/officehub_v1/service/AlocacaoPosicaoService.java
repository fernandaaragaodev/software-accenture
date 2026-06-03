package com.accenture.officehub_v1.service;

import com.accenture.officehub_v1.dto.request.PessoaReservaRequest;
import com.accenture.officehub_v1.entity.Posicao;
import com.accenture.officehub_v1.service.alocacao.ItemAlocacao;
import com.accenture.officehub_v1.service.alocacao.ResultadoAlocacao;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class AlocacaoPosicaoService {

    public ResultadoAlocacao alocar(
            List<PessoaReservaRequest> pessoas,
            List<Posicao> posicoesLivres,
            String criterioProximidade,
            BigDecimal raioProximidade) {

        if (posicoesLivres.isEmpty()) {
            return ResultadoAlocacao.falha("Não há posições livres na data solicitada.");
        }

        if (CriterioProximidade.isObrigatoria(criterioProximidade)) {
            return alocarComProximidadeObrigatoria(pessoas, posicoesLivres, raioProximidade);
        }

        return alocarPreferencialOuSimples(pessoas, posicoesLivres, criterioProximidade);
    }

    private ResultadoAlocacao alocarPreferencialOuSimples(
            List<PessoaReservaRequest> pessoas,
            List<Posicao> posicoesLivres,
            String criterioProximidade) {

        List<Posicao> disponiveis = ordenarPosicoes(posicoesLivres);
        Set<UUID> usadas = new HashSet<>();
        List<Posicao> atribuidas = new ArrayList<>();
        List<ItemAlocacao> resultado = new ArrayList<>();

        for (PessoaReservaRequest pessoa : pessoas) {
            List<Posicao> candidatas = filtrarCompativeis(disponiveis, pessoa, usadas);

            if (candidatas.isEmpty()) {
                return ResultadoAlocacao.falha(montarMotivoSemCompatibilidade(pessoa));
            }

            Posicao escolhida;
            if (CriterioProximidade.isPreferencial(criterioProximidade) && !atribuidas.isEmpty()) {
                escolhida = escolherMaisProxima(candidatas, atribuidas);
            } else {
                escolhida = escolherMelhorRank(candidatas, pessoa);
            }

            usadas.add(escolhida.getId());
            atribuidas.add(escolhida);
            resultado.add(new ItemAlocacao(pessoa, escolhida));
        }

        return ResultadoAlocacao.sucesso(resultado);
    }

    private ResultadoAlocacao alocarComProximidadeObrigatoria(
            List<PessoaReservaRequest> pessoas,
            List<Posicao> posicoesLivres,
            BigDecimal raioProximidade) {

        if (raioProximidade == null) {
            return ResultadoAlocacao.falha(
                    "A sala não possui raio de proximidade configurado para alocação obrigatória.");
        }

        double raio = raioProximidade.doubleValue();
        List<Posicao> disponiveis = ordenarPosicoes(posicoesLivres);

        Optional<List<Posicao>> alocacao = buscarCombinacaoObrigatoria(
                pessoas, disponiveis, raio, 0, new ArrayList<>(), new HashSet<>());

        if (alocacao.isEmpty()) {
            return ResultadoAlocacao.falha(
                    "As posições compatíveis disponíveis não satisfazem o critério de proximidade obrigatória.");
        }

        List<ItemAlocacao> resultado = new ArrayList<>();
        List<Posicao> posicoesAlocadas = alocacao.get();

        for (int i = 0; i < pessoas.size(); i++) {
            resultado.add(new ItemAlocacao(pessoas.get(i), posicoesAlocadas.get(i)));
        }

        return ResultadoAlocacao.sucesso(resultado);
    }

    private Optional<List<Posicao>> buscarCombinacaoObrigatoria(
            List<PessoaReservaRequest> pessoas,
            List<Posicao> disponiveis,
            double raio,
            int indice,
            List<Posicao> atribuidas,
            Set<UUID> usadas) {

        if (indice == pessoas.size()) {
            return grupoRespeitaRaio(atribuidas, raio)
                    ? Optional.of(new ArrayList<>(atribuidas))
                    : Optional.empty();
        }

        PessoaReservaRequest pessoa = pessoas.get(indice);
        List<Posicao> candidatas = filtrarCompativeis(disponiveis, pessoa, usadas);

        candidatas = atribuidas.isEmpty()
                ? ordenarPorRank(candidatas, pessoa)
                : ordenarPorProximidade(candidatas, atribuidas);

        for (Posicao candidata : candidatas) {
            if (!possuiCoordenadas(candidata)) {
                continue;
            }

            usadas.add(candidata.getId());
            atribuidas.add(candidata);

            Optional<List<Posicao>> solucao = buscarCombinacaoObrigatoria(
                    pessoas, disponiveis, raio, indice + 1, atribuidas, usadas);

            if (solucao.isPresent()) {
                return solucao;
            }

            atribuidas.remove(atribuidas.size() - 1);
            usadas.remove(candidata.getId());
        }

        return Optional.empty();
    }

    private List<Posicao> filtrarCompativeis(
            List<Posicao> posicoes,
            PessoaReservaRequest pessoa,
            Set<UUID> usadas) {

        return posicoes.stream()
                .filter(p -> !usadas.contains(p.getId()))
                .filter(p -> posicaoCompativel(p, pessoa))
                .toList();
    }

    boolean posicaoCompativel(Posicao posicao, PessoaReservaRequest pessoa) {
        List<String> preferencias = Stream.of(
                        pessoa.tipoPreferido1(),
                        pessoa.tipoPreferido2(),
                        pessoa.tipoPreferido3())
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        if (preferencias.isEmpty()) {
            return true;
        }

        if (posicao.getTipo() == null || posicao.getTipo().isBlank()) {
            return false;
        }

        String tipoPosicao = posicao.getTipo().trim();

        return preferencias.stream()
                .anyMatch(pref -> pref.equalsIgnoreCase(tipoPosicao));
    }

    private Posicao escolherMelhorRank(List<Posicao> candidatas, PessoaReservaRequest pessoa) {
        return ordenarPorRank(candidatas, pessoa).get(0);
    }

    private Posicao escolherMaisProxima(List<Posicao> candidatas, List<Posicao> atribuidas) {
        if (atribuidas.isEmpty()) {
            return candidatas.get(0);
        }

        return ordenarPorProximidade(candidatas, atribuidas).get(0);
    }

    private List<Posicao> ordenarPorRank(List<Posicao> candidatas, PessoaReservaRequest pessoa) {
        return candidatas.stream()
                .sorted(Comparator
                        .comparingInt((Posicao p) -> rankPreferencia(p, pessoa))
                        .thenComparing(
                                Posicao::getIdentificador,
                                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();
    }

    private List<Posicao> ordenarPorProximidade(List<Posicao> candidatas, List<Posicao> referencias) {
        return candidatas.stream()
                .sorted(Comparator
                        .comparingDouble((Posicao c) -> distanciaMinima(c, referencias))
                        .thenComparing(
                                Posicao::getIdentificador,
                                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();
    }

    private int rankPreferencia(Posicao posicao, PessoaReservaRequest pessoa) {
        if (posicao.getTipo() == null) {
            return Integer.MAX_VALUE;
        }

        String tipo = posicao.getTipo().trim();

        if (equalsIgnoreCase(pessoa.tipoPreferido1(), tipo)) {
            return 1;
        }

        if (equalsIgnoreCase(pessoa.tipoPreferido2(), tipo)) {
            return 2;
        }

        if (equalsIgnoreCase(pessoa.tipoPreferido3(), tipo)) {
            return 3;
        }

        return Integer.MAX_VALUE;
    }

    private boolean equalsIgnoreCase(String a, String b) {
        return a != null && b != null && a.trim().equalsIgnoreCase(b.trim());
    }

    private double distanciaMinima(Posicao candidata, List<Posicao> referencias) {
        return referencias.stream()
                .mapToDouble(ref -> distancia(candidata, ref))
                .min()
                .orElse(Double.MAX_VALUE);
    }

    private boolean grupoRespeitaRaio(List<Posicao> posicoes, double raio) {
        if (posicoes.size() <= 1) {
            return posicoes.stream().allMatch(this::possuiCoordenadas);
        }

        for (int i = 0; i < posicoes.size(); i++) {
            for (int j = i + 1; j < posicoes.size(); j++) {
                if (distancia(posicoes.get(i), posicoes.get(j)) > raio) {
                    return false;
                }
            }
        }

        return true;
    }

    double distancia(Posicao a, Posicao b) {
        if (!possuiCoordenadas(a) || !possuiCoordenadas(b)) {
            return Double.POSITIVE_INFINITY;
        }

        double dx = a.getCoordX().doubleValue() - b.getCoordX().doubleValue();
        double dy = a.getCoordY().doubleValue() - b.getCoordY().doubleValue();

        return Math.sqrt(dx * dx + dy * dy);
    }

    private boolean possuiCoordenadas(Posicao posicao) {
        return posicao.getCoordX() != null && posicao.getCoordY() != null;
    }

    private List<Posicao> ordenarPosicoes(List<Posicao> posicoes) {
        return posicoes.stream()
                .sorted(Comparator.comparing(
                        Posicao::getIdentificador,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();
    }

    private String montarMotivoSemCompatibilidade(PessoaReservaRequest pessoa) {
        String preferencias = Stream.of(
                        pessoa.tipoPreferido1(),
                        pessoa.tipoPreferido2(),
                        pessoa.tipoPreferido3())
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .reduce((a, b) -> a + ", " + b)
                .orElse("não informado");

        return "Não há posições livres compatíveis para os tipos preferidos: " + preferencias + ".";
    }
}