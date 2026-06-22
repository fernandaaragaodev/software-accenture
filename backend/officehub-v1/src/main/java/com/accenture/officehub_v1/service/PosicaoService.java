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

        return salvarPosicao(request, sala, layout);
    }

    @Transactional
    public PosicaoResponse criarNoLayout(CriarPosicaoRequest request, UUID layoutId) {
        Sala sala = salaService.buscarEntidadeAtiva(request.salaId());
        validarIdentificadorDuplicado(sala.getId(), request.identificador(), null);

        Layout layout = layoutRepository.findById(layoutId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Layout não encontrado."));

        if (!layout.getSala().getId().equals(sala.getId())) {
            throw new RegraNegocioException("O layout informado não pertence à sala.");
        }

        return salvarPosicao(request, sala, layout);
    }

    private PosicaoResponse salvarPosicao(CriarPosicaoRequest request, Sala sala, Layout layout) {
        Posicao posicao = Posicao.builder()
                .sala(sala)
                .layout(layout)
                .identificador(request.identificador())
                .tipo(request.tipo())
                .coordX(request.coordX())
                .coordY(request.coordY())
                .pixelX(request.pixelX())
                .pixelY(request.pixelY())
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
        return posicaoRepository.findBySalaIdOrderByIdentificadorAsc(salaId).stream()
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
    public PosicaoResponse bloquear(UUID id) {
        Posicao posicao = buscarEntidade(id);
        if (!PosicaoStatus.isAtiva(posicao)) {
            throw new RegraNegocioException("A posição já está bloqueada.");
        }

        posicao.setStatus(PosicaoStatus.INATIVA);
        Posicao posicaoSalva = posicaoRepository.save(posicao);
        auditService.registrar(SecurityUtils.getUsuarioIdAtual(), "BLOQUEAR", "Posicao", posicaoSalva.getId());
        return PosicaoResponse.from(posicaoSalva);
    }

    @Transactional
    public PosicaoResponse desbloquear(UUID id) {
        Posicao posicao = buscarEntidade(id);
        if (PosicaoStatus.isAtiva(posicao)) {
            throw new RegraNegocioException("A posição já está ativa.");
        }

        validarIdentificadorDuplicado(
                posicao.getSala().getId(),
                posicao.getIdentificador(),
                posicao.getId());

        posicao.setStatus(PosicaoStatus.ATIVA);
        Posicao posicaoSalva = posicaoRepository.save(posicao);
        auditService.registrar(SecurityUtils.getUsuarioIdAtual(), "DESBLOQUEAR", "Posicao", posicaoSalva.getId());
        return PosicaoResponse.from(posicaoSalva);
    }

    @Transactional
    public PosicaoResponse inativar(UUID id) {
        return bloquear(id);
    }

    @Transactional
    public PosicaoResponse reativar(UUID id) {
        return desbloquear(id);
    }

    public Posicao buscarEntidadeAtiva(UUID id) {
        Posicao posicao = buscarEntidade(id);
        if (!PosicaoStatus.isAtiva(posicao)) {
            throw new RecursoNaoEncontradoException(
                    "Posição não encontrada ou foi bloqueada.");
        }
        return posicao;
    }

    public Posicao buscarEntidade(UUID id) {
        return posicaoRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Posição não encontrada."));
    }

    public List<Posicao> listarPosicoesAtivasDaSala(UUID salaId) {
        Layout layoutAtivo = layoutRepository.findBySalaIdAndAtivoTrue(salaId).orElse(null);
        if (layoutAtivo == null) {
            return List.of();
        }

        return posicaoRepository.findBySalaIdAndDeletedAtIsNull(salaId).stream()
                .filter(PosicaoStatus::isAtiva)
                .filter(p -> p.getLayout() != null && layoutAtivo.getId().equals(p.getLayout().getId()))
                .toList();
    }

    public List<Posicao> listarPosicoesDaSala(UUID salaId) {
        salaService.buscarEntidadeAtiva(salaId);
        return posicaoRepository.findBySalaIdAndDeletedAtIsNull(salaId);
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
