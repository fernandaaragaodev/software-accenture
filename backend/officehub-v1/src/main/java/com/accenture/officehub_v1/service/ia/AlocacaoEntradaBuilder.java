package com.accenture.officehub_v1.service.ia;

import com.accenture.officehub_v1.dto.ia.AlocacaoAgenteEntradaDto;
import com.accenture.officehub_v1.dto.ia.PessoaAlocacaoEntradaDto;
import com.accenture.officehub_v1.dto.ia.PosicaoLivreEntradaDto;
import com.accenture.officehub_v1.dto.request.PessoaReservaRequest;
import com.accenture.officehub_v1.entity.Posicao;
import com.accenture.officehub_v1.entity.Sala;
import com.accenture.officehub_v1.entity.Usuario;
import com.accenture.officehub_v1.entity.enums.TipoPessoaAlocacao;
import com.accenture.officehub_v1.exception.RecursoNaoEncontradoException;
import com.accenture.officehub_v1.repository.EquipeGestorRepository;
import com.accenture.officehub_v1.repository.EquipeMembroRepository;
import com.accenture.officehub_v1.repository.PosicaoEquipamentoRepository;
import com.accenture.officehub_v1.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class AlocacaoEntradaBuilder {

    private final UsuarioRepository usuarioRepository;
    private final EquipeGestorRepository equipeGestorRepository;
    private final EquipeMembroRepository equipeMembroRepository;
    private final PosicaoEquipamentoRepository posicaoEquipamentoRepository;

    public AlocacaoAgenteEntradaDto montar(
            Sala sala,
            LocalDate dataReserva,
            UUID equipeId,
            List<PessoaReservaRequest> pessoas,
            String criterioProximidade,
            List<Posicao> posicoesLivres) {

        Map<UUID, List<String>> equipamentosPorPosicao = carregarEquipamentosPorPosicao(posicoesLivres);

        List<PosicaoLivreEntradaDto> posicoesEntrada = posicoesLivres.stream()
                .map(p -> new PosicaoLivreEntradaDto(
                        p.getId(),
                        p.getCoordX(),
                        p.getCoordY(),
                        p.getTipo(),
                        equipamentosPorPosicao.getOrDefault(p.getId(), List.of())))
                .toList();

        BigDecimal[] entrada = calcularCoordenadasEntrada(posicoesLivres);
        UUID liderEquipeId = resolverLiderEquipe(equipeId);

        List<PessoaAlocacaoEntradaDto> pessoasEntrada = pessoas.stream()
                .map(p -> montarPessoa(p, equipeId, liderEquipeId))
                .toList();

        return new AlocacaoAgenteEntradaDto(
                sala.getId(),
                dataReserva,
                equipeId != null ? "EQUIPE" : "INDIVIDUAL",
                criterioProximidade,
                sala.getRaioProximidade(),
                sala.getCapacidadeMaxima(),
                entrada[0],
                entrada[1],
                pessoasEntrada,
                posicoesEntrada);
    }

    private PessoaAlocacaoEntradaDto montarPessoa(
            PessoaReservaRequest pessoa,
            UUID equipeId,
            UUID liderEquipeId) {

        Usuario usuario = usuarioRepository.findByIdAndDeletedAtIsNull(pessoa.usuarioId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Usuário participante não encontrado: " + pessoa.usuarioId()));

        List<String> preferencias = tiposCompativeis(pessoa);

        return new PessoaAlocacaoEntradaDto(
                pessoa.usuarioId(),
                usuario.getNome(),
                resolverTipoPessoa(pessoa.usuarioId(), equipeId, liderEquipeId),
                equipeId,
                preferencias,
                preferencias);
    }

    TipoPessoaAlocacao resolverTipoPessoa(UUID usuarioId, UUID equipeId, UUID liderEquipeId) {
        if (equipeId == null) {
            return TipoPessoaAlocacao.FUNCIONARIO;
        }

        if (liderEquipeId != null && liderEquipeId.equals(usuarioId)) {
            return TipoPessoaAlocacao.LIDER_EQUIPE;
        }

        if (equipeGestorRepository.existsByEquipeIdAndUsuarioId(equipeId, usuarioId)) {
            return TipoPessoaAlocacao.GESTOR;
        }

        if (equipeMembroRepository.existsByEquipeIdAndUsuarioId(equipeId, usuarioId)) {
            return TipoPessoaAlocacao.FUNCIONARIO;
        }

        return TipoPessoaAlocacao.VISITANTE;
    }

    private UUID resolverLiderEquipe(UUID equipeId) {
        if (equipeId == null) {
            return null;
        }

        return equipeGestorRepository.findByEquipeId(equipeId).stream()
                .map(eg -> eg.getUsuario().getId())
                .findFirst()
                .orElse(null);
    }

    private BigDecimal[] calcularCoordenadasEntrada(List<Posicao> posicoes) {
        BigDecimal minX = posicoes.stream()
                .map(Posicao::getCoordX)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);

        BigDecimal minY = posicoes.stream()
                .map(Posicao::getCoordY)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);

        return new BigDecimal[]{minX, minY};
    }

    private Map<UUID, List<String>> carregarEquipamentosPorPosicao(List<Posicao> posicoes) {
        if (posicoes.isEmpty()) {
            return Map.of();
        }

        List<UUID> posicaoIds = posicoes.stream().map(Posicao::getId).toList();
        return posicaoEquipamentoRepository.findByPosicaoIdInWithTipoEquipamento(posicaoIds).stream()
                .collect(Collectors.groupingBy(
                        pe -> pe.getPosicao().getId(),
                        Collectors.mapping(pe -> pe.getTipoEquipamento().getNome(), Collectors.toList())));
    }

    private List<String> tiposCompativeis(PessoaReservaRequest pessoa) {
        List<String> tipos = new ArrayList<>();
        Stream.of(pessoa.tipoPreferido1(), pessoa.tipoPreferido2(), pessoa.tipoPreferido3())
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .forEach(tipos::add);
        return tipos;
    }
}
