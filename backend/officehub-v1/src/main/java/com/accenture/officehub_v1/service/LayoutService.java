package com.accenture.officehub_v1.service;

import com.accenture.officehub_v1.dto.request.AtualizarCoordenadasPosicaoRequest;
import com.accenture.officehub_v1.dto.request.CriarLayoutRequest;
import com.accenture.officehub_v1.dto.response.LayoutResponse;
import com.accenture.officehub_v1.entity.Layout;
import com.accenture.officehub_v1.entity.Sala;
import com.accenture.officehub_v1.entity.Usuario;
import com.accenture.officehub_v1.exception.RecursoNaoEncontradoException;
import com.accenture.officehub_v1.exception.RegraNegocioException;
import com.accenture.officehub_v1.repository.LayoutRepository;
import com.accenture.officehub_v1.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LayoutService {

    private final LayoutRepository layoutRepository;
    private final SalaService salaService;
    private final PosicaoService posicaoService;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public LayoutResponse criar(CriarLayoutRequest request) {
        Sala sala = salaService.buscarEntidadeAtiva(request.salaId());

        Layout layout = Layout.builder()
                .sala(sala)
                .versao(request.versao())
                .ativo(false)
                .build();

        return LayoutResponse.from(layoutRepository.save(layout));
    }

    public LayoutResponse buscarLayoutAtivo(UUID salaId) {
        salaService.buscarEntidadeAtiva(salaId);
        return LayoutResponse.from(buscarLayoutAtivoEntidade(salaId));
    }

    public void validarLayoutAtivoAprovado(UUID salaId) {
        Layout layout = buscarLayoutAtivoEntidade(salaId);

        if (layout.getAprovadoPor() == null || layout.getAprovadoEm() == null) {
            throw new RegraNegocioException(
                    "A sala não possui layout ativo e aprovado para alocação de posições.");
        }
    }

    public Layout buscarLayoutAtivoEntidade(UUID salaId) {
        salaService.buscarEntidadeAtiva(salaId);

        return layoutRepository.findBySalaIdAndAtivoTrue(salaId)
                .orElseThrow(() -> new RegraNegocioException(
                        "A sala não possui layout ativo para alocação de posições."));
    }

    @Transactional
    public LayoutResponse aprovar(UUID layoutId, UUID aprovadoPorId) {
        Layout layout = layoutRepository.findById(layoutId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Layout não encontrado."));

        Usuario aprovador = usuarioRepository.findByIdAndDeletedAtIsNull(aprovadoPorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Usuário aprovador não encontrado."));

        desativarLayoutsAtivos(layout.getSala().getId(), layoutId);

        layout.setAtivo(true);
        layout.setAprovadoPor(aprovador);
        layout.setAprovadoEm(OffsetDateTime.now());

        return LayoutResponse.from(layoutRepository.save(layout));
    }

    @Transactional
    public void ajustarCoordenadasPosicao(UUID posicaoId, AtualizarCoordenadasPosicaoRequest request) {
        posicaoService.atualizarCoordenadas(posicaoId, request);
    }

    private void desativarLayoutsAtivos(UUID salaId, UUID layoutAtualId) {
        List<Layout> layoutsAtivos = layoutRepository.findBySalaId(salaId).stream()
                .filter(Layout::getAtivo)
                .filter(l -> !l.getId().equals(layoutAtualId))
                .toList();

        for (Layout layoutAtivo : layoutsAtivos) {
            layoutAtivo.setAtivo(false);
            layoutRepository.save(layoutAtivo);
        }
    }
}
