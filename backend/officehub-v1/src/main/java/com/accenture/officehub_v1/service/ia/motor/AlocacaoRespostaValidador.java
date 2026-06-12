package com.accenture.officehub_v1.service.ia.motor;

import com.accenture.officehub_v1.dto.ia.AlocacaoAgenteEntradaDto;
import com.accenture.officehub_v1.dto.ia.AlocacaoAgenteSaidaDto;
import com.accenture.officehub_v1.dto.ia.PessoaAlocacaoEntradaDto;
import com.accenture.officehub_v1.dto.ia.PosicaoAlocadaSaidaDto;
import com.accenture.officehub_v1.dto.ia.PosicaoLivreEntradaDto;
import com.accenture.officehub_v1.service.ia.PosicaoCompatibilidade;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class AlocacaoRespostaValidador {

    public static final String ERRO_COMBINACAO_JA_SUGERIDA =
            "A IA retornou uma combinação já sugerida anteriormente.";

    public Optional<String> validar(AlocacaoAgenteEntradaDto entrada, AlocacaoAgenteSaidaDto saida) {
        return validar(entrada, saida, false);
    }

    public Optional<String> validar(
            AlocacaoAgenteEntradaDto entrada,
            AlocacaoAgenteSaidaDto saida,
            boolean ignorarCombinacoesExcluidas) {
        if (saida == null) {
            return Optional.of("Resposta da IA ausente.");
        }

        if (!saida.sucesso()) {
            return Optional.of(saida.motivoFalha() != null
                    ? saida.motivoFalha()
                    : "A IA não conseguiu alocar os participantes.");
        }

        if (saida.alocacoes() == null || saida.alocacoes().isEmpty()) {
            return Optional.of("A IA retornou alocação vazia.");
        }

        if (saida.alocacoes().size() != entrada.pessoas().size()) {
            return Optional.of("A IA não alocou todos os participantes da reserva.");
        }

        Set<UUID> pessoasEsperadas = entrada.pessoas().stream()
                .map(PessoaAlocacaoEntradaDto::id)
                .collect(Collectors.toSet());

        Map<UUID, PosicaoLivreEntradaDto> posicoesLivres = entrada.posicoesLivres().stream()
                .collect(Collectors.toMap(PosicaoLivreEntradaDto::id, Function.identity()));

        Set<UUID> pessoasAlocadas = new HashSet<>();
        Set<UUID> posicoesUsadas = new HashSet<>();

        for (PosicaoAlocadaSaidaDto alocacao : saida.alocacoes()) {
            if (alocacao.pessoaId() == null || alocacao.posicaoId() == null) {
                return Optional.of("A IA retornou alocação com identificadores inválidos.");
            }

            if (!pessoasEsperadas.contains(alocacao.pessoaId())) {
                return Optional.of("A IA alocou pessoa não pertencente à reserva.");
            }

            if (!pessoasAlocadas.add(alocacao.pessoaId())) {
                return Optional.of("A IA duplicou pessoa na alocação.");
            }

            if (!posicoesUsadas.add(alocacao.posicaoId())) {
                return Optional.of("A IA duplicou posição na alocação.");
            }

            if (!posicoesLivres.containsKey(alocacao.posicaoId())) {
                return Optional.of("A IA utilizou posição ocupada, inexistente ou indisponível.");
            }
        }

        for (PessoaAlocacaoEntradaDto pessoa : entrada.pessoas()) {
            UUID posicaoId = saida.alocacoes().stream()
                    .filter(a -> a.pessoaId().equals(pessoa.id()))
                    .map(PosicaoAlocadaSaidaDto::posicaoId)
                    .findFirst()
                    .orElse(null);

            if (posicaoId == null) {
                return Optional.of("A IA não alocou todos os participantes da reserva.");
            }

            PosicaoLivreEntradaDto posicao = posicoesLivres.get(posicaoId);
            if (!posicaoCompativel(posicao, pessoa)) {
                return Optional.of("A IA alocou posição incompatível com as preferências do participante.");
            }
        }

        if (!ignorarCombinacoesExcluidas
                && combinacaoExcluida(entrada.combinacoesExcluidas(), posicoesUsadas)) {
            return Optional.of(ERRO_COMBINACAO_JA_SUGERIDA);
        }

        return Optional.empty();
    }

    public boolean alternativasEsgotadas(AlocacaoAgenteEntradaDto entrada) {
        if (entrada.combinacoesExcluidas() == null || entrada.combinacoesExcluidas().isEmpty()) {
            return false;
        }

        if (entrada.pessoas().size() != 1) {
            return false;
        }

        PessoaAlocacaoEntradaDto pessoa = entrada.pessoas().get(0);
        long posicoesCompativeis = entrada.posicoesLivres().stream()
                .filter(posicao -> posicaoCompativel(posicao, pessoa))
                .count();

        return posicoesCompativeis > 0
                && entrada.combinacoesExcluidas().size() >= posicoesCompativeis;
    }

    public boolean combinacaoExcluida(AlocacaoAgenteEntradaDto entrada, AlocacaoAgenteSaidaDto saida) {
        if (!saida.sucesso() || saida.alocacoes() == null) {
            return false;
        }

        Set<UUID> posicoesUsadas = saida.alocacoes().stream()
                .map(PosicaoAlocadaSaidaDto::posicaoId)
                .collect(Collectors.toSet());

        return combinacaoExcluida(entrada.combinacoesExcluidas(), posicoesUsadas);
    }

    private boolean combinacaoExcluida(List<List<UUID>> combinacoesExcluidas, Set<UUID> posicoesUsadas) {
        if (combinacoesExcluidas == null || combinacoesExcluidas.isEmpty()) {
            return false;
        }

        for (List<UUID> excluida : combinacoesExcluidas) {
            if (excluida != null && new HashSet<>(excluida).equals(posicoesUsadas)) {
                return true;
            }
        }

        return false;
    }

    private boolean posicaoCompativel(PosicaoLivreEntradaDto posicao, PessoaAlocacaoEntradaDto pessoa) {
        return PosicaoCompatibilidade.compativel(
                posicao.tipo(),
                posicao.equipamentos(),
                pessoa.tiposPosicaoCompativeis());
    }
}
