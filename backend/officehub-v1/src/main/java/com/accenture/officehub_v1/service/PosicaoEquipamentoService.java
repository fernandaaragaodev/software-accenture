package com.accenture.officehub_v1.service;

import com.accenture.officehub_v1.dto.request.VincularEquipamentoPosicaoRequest;
import com.accenture.officehub_v1.dto.response.PosicaoEquipamentoResponse;
import com.accenture.officehub_v1.entity.Posicao;
import com.accenture.officehub_v1.entity.PosicaoEquipamento;
import com.accenture.officehub_v1.entity.TipoEquipamento;
import com.accenture.officehub_v1.exception.RecursoNaoEncontradoException;
import com.accenture.officehub_v1.exception.RegraNegocioException;
import com.accenture.officehub_v1.repository.PosicaoEquipamentoRepository;
import com.accenture.officehub_v1.repository.TipoEquipamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PosicaoEquipamentoService {

    private final PosicaoEquipamentoRepository posicaoEquipamentoRepository;
    private final TipoEquipamentoRepository tipoEquipamentoRepository;
    private final PosicaoService posicaoService;
    private final TipoEquipamentoService tipoEquipamentoService;

    @Transactional
    public PosicaoEquipamentoResponse vincular(UUID posicaoId, VincularEquipamentoPosicaoRequest request) {
        Posicao posicao = posicaoService.buscarEntidadeAtiva(posicaoId);
        TipoEquipamento tipo = buscarTipoEquipamentoAtivo(request.tipoEquipamentoId());

        if (posicaoEquipamentoRepository.existsByPosicaoIdAndTipoEquipamentoId(
                posicaoId, request.tipoEquipamentoId())) {
            throw new RegraNegocioException(
                    "Este tipo de equipamento já está vinculado à posição.");
        }

        PosicaoEquipamento vinculo = PosicaoEquipamento.builder()
                .posicao(posicao)
                .tipoEquipamento(tipo)
                .quantidade(request.quantidade())
                .observacao(request.observacao())
                .build();

        return PosicaoEquipamentoResponse.from(posicaoEquipamentoRepository.save(vinculo));
    }

    @Transactional
    public PosicaoEquipamentoResponse vincularPorTipoNome(UUID posicaoId, String tipoEquipamentoNome, String observacao) {
        Posicao posicao = posicaoService.buscarEntidadeAtiva(posicaoId);
        TipoEquipamento tipo = tipoEquipamentoService.buscarOuCriarPorNome(
                tipoEquipamentoNome,
                observacao != null ? observacao : "Equipamento detectado automaticamente via IA");

        return posicaoEquipamentoRepository.findByPosicaoIdOrderByCreatedAtAsc(posicaoId).stream()
                .filter(v -> v.getTipoEquipamento().getId().equals(tipo.getId()))
                .findFirst()
                .map(PosicaoEquipamentoResponse::from)
                .orElseGet(() -> {
                    PosicaoEquipamento vinculo = PosicaoEquipamento.builder()
                            .posicao(posicao)
                            .tipoEquipamento(tipo)
                            .quantidade(1)
                            .observacao(observacao)
                            .build();
                    return PosicaoEquipamentoResponse.from(posicaoEquipamentoRepository.save(vinculo));
                });
    }

    public List<PosicaoEquipamentoResponse> listarPorPosicao(UUID posicaoId) {
        posicaoService.buscarEntidadeAtiva(posicaoId);

        return posicaoEquipamentoRepository.findByPosicaoIdOrderByCreatedAtAsc(posicaoId).stream()
                .map(PosicaoEquipamentoResponse::from)
                .toList();
    }

    private TipoEquipamento buscarTipoEquipamentoAtivo(UUID tipoEquipamentoId) {
        return tipoEquipamentoRepository.findByIdAndAtivoTrue(tipoEquipamentoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Tipo de equipamento não encontrado ou está inativo."));
    }
}
