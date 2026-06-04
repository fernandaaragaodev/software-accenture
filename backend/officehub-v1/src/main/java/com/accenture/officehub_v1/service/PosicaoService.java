package com.accenture.officehub_v1.service;

import com.accenture.officehub_v1.dto.request.AtualizarCoordenadasPosicaoRequest;
import com.accenture.officehub_v1.dto.request.CriarPosicaoRequest;
import com.accenture.officehub_v1.dto.response.PosicaoResponse;
import com.accenture.officehub_v1.entity.Layout;
import com.accenture.officehub_v1.entity.Posicao;
import com.accenture.officehub_v1.entity.Sala;
import com.accenture.officehub_v1.exception.RecursoNaoEncontradoException;
import com.accenture.officehub_v1.exception.RegraNegocioException;
import com.accenture.officehub_v1.repository.LayoutRepository;
import com.accenture.officehub_v1.repository.PosicaoRepository;
import com.accenture.officehub_v1.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PosicaoService {

    private final PosicaoRepository posicaoRepository;
    private final SalaService salaService;
    private final LayoutRepository layoutRepository;
    private final AuditService auditService;

    @Transactional
    public PosicaoResponse criar(CriarPosicaoRequest request) {
        Sala sala = salaService.buscarEntidadeAtiva(request.salaId());
        validarIdentificadorDuplicado(sala.getId(), request.identificador(), null);

        Layout layout = layoutRepository.findBySalaIdAndAtivoTrue(sala.getId())
                .orElseThrow(() -> new RegraNegocioException(
                        "A sala não possui layout ativo. Cadastre e aprove um layout antes de criar posições."));

        Posicao posicao = Posicao.builder()
                .sala(sala)
                .layout(layout)
                .identificador(request.identificador())
                .tipo(request.tipo())
                .coordX(request.coordX())
                .coordY(request.coordY())
                .tipoCadeira(request.tipoCadeira())
                .tipoMesa(request.tipoMesa())
                .status(PosicaoStatus.ATIVA)
                .build();

        Posicao posicaoSalva = posicaoRepository.save(posicao);
        auditService.registrar(SecurityUtils.getUsuarioIdAtual(), "CRIAR", "Posicao", posicaoSalva.getId());
        return PosicaoResponse.from(posicaoSalva);
    }

    public List<PosicaoResponse> listarPorSala(UUID salaId) {
        salaService.buscarEntidadeAtiva(salaId);
        return posicaoRepository.findBySalaIdAndDeletedAtIsNull(salaId).stream()
                .map(PosicaoResponse::from)
                .toList();
    }

    public PosicaoResponse buscarPorId(UUID id) {
        return PosicaoResponse.from(buscarEntidadeAtiva(id));
    }

    @Transactional
    public PosicaoResponse atualizarCoordenadas(UUID id, AtualizarCoordenadasPosicaoRequest request) {
        Posicao posicao = buscarEntidadeAtiva(id);
        posicao.setCoordX(request.coordX());
        posicao.setCoordY(request.coordY());
        posicao.setAjustadoManualmente(true);
        Posicao posicaoSalva = posicaoRepository.save(posicao);
        auditService.registrar(SecurityUtils.getUsuarioIdAtual(), "ATUALIZAR", "Posicao", posicaoSalva.getId());
        return PosicaoResponse.from(posicaoSalva);
    }

    @Transactional
    public PosicaoResponse inativar(UUID id) {
        Posicao posicao = buscarEntidadeAtiva(id);
        posicao.setStatus(PosicaoStatus.INATIVA);
        posicao.setDeletedAt(OffsetDateTime.now());
        return PosicaoResponse.from(posicaoRepository.save(posicao));
    }

    public Posicao buscarEntidadeAtiva(UUID id) {
        return posicaoRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Posição não encontrada ou foi inativada."));
    }

    public List<Posicao> listarPosicoesAtivasDaSala(UUID salaId) {
        return posicaoRepository.findBySalaIdAndDeletedAtIsNull(salaId).stream()
                .filter(p -> PosicaoStatus.ATIVA.equals(p.getStatus()))
                .toList();
    }

    private void validarIdentificadorDuplicado(UUID salaId, String identificador, UUID idExcluir) {
        boolean duplicado = idExcluir == null
                ? posicaoRepository.existsBySalaIdAndIdentificadorIgnoreCaseAndDeletedAtIsNull(salaId, identificador)
                : posicaoRepository.existsBySalaIdAndIdentificadorIgnoreCaseAndDeletedAtIsNullAndIdNot(
                        salaId, identificador, idExcluir);

        if (duplicado) {
            throw new RegraNegocioException(
                    "Já existe uma posição com este identificador nesta sala.");
        }
    }
}
