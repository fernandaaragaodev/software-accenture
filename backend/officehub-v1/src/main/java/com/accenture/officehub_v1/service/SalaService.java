package com.accenture.officehub_v1.service;

import com.accenture.officehub_v1.dto.request.AtualizarSalaRequest;
import com.accenture.officehub_v1.dto.request.AtualizarStatusSalaRequest;
import com.accenture.officehub_v1.dto.request.CriarSalaRequest;
import com.accenture.officehub_v1.dto.response.SalaResponse;
import com.accenture.officehub_v1.entity.Sala;
import com.accenture.officehub_v1.entity.Usuario;
import com.accenture.officehub_v1.entity.enums.StatusSala;
import com.accenture.officehub_v1.exception.RecursoNaoEncontradoException;
import com.accenture.officehub_v1.exception.RegraNegocioException;
import com.accenture.officehub_v1.repository.SalaRepository;
import com.accenture.officehub_v1.repository.UsuarioRepository;
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
public class SalaService {

    private final SalaRepository salaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditService auditService;

    @Transactional
    public SalaResponse criar(CriarSalaRequest request, UUID createdById) {
        validarNomeDuplicado(request.nome(), null);

        Usuario createdBy = usuarioRepository.findByIdAndDeletedAtIsNull(createdById)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Usuário responsável pelo cadastro não encontrado."));

        Sala sala = Sala.builder()
                .nome(request.nome())
                .descricao(request.descricao())
                .andar(request.andar())
                .bloco(request.bloco())
                .capacidadeMaxima(request.capacidadeMaxima())
                .raioProximidade(request.raioProximidade())
                .imagemPath(request.imagemPath())
                .status(StatusSala.ATIVA)
                .createdBy(createdBy)
                .build();

        Sala salaSalva = salaRepository.save(sala);
        auditService.registrar(createdById, "CRIAR", "Sala", salaSalva.getId());
        return SalaResponse.from(salaSalva);
    }

    public List<SalaResponse> listarNaoDeletadas() {
        return salaRepository.findByDeletedAtIsNull().stream()
                .map(SalaResponse::from)
                .toList();
    }

    public SalaResponse buscarPorId(UUID id) {
        return SalaResponse.from(buscarEntidadeAtiva(id));
    }

    @Transactional
    public SalaResponse atualizar(UUID id, AtualizarSalaRequest request) {
        Sala sala = buscarEntidadeAtiva(id);
        validarNomeDuplicado(request.nome(), id);

        sala.setNome(request.nome());
        sala.setDescricao(request.descricao());
        sala.setAndar(request.andar());
        sala.setBloco(request.bloco());
        sala.setCapacidadeMaxima(request.capacidadeMaxima());
        sala.setRaioProximidade(request.raioProximidade());
        sala.setImagemPath(request.imagemPath());

        Sala salaSalva = salaRepository.save(sala);
        auditService.registrar(SecurityUtils.getUsuarioIdAtual(), "ATUALIZAR", "Sala", salaSalva.getId());
        return SalaResponse.from(salaSalva);
    }

    @Transactional
    public SalaResponse inativar(UUID id) {
        Sala sala = buscarEntidadeAtiva(id);
        sala.setStatus(StatusSala.INATIVA);
        sala.setDeletedAt(OffsetDateTime.now());
        return SalaResponse.from(salaRepository.save(sala));
    }

    @Transactional
    public SalaResponse atualizarStatus(UUID id, AtualizarStatusSalaRequest request) {
        Sala sala = buscarEntidadeAtiva(id);
        sala.setStatus(request.status());
        return SalaResponse.from(salaRepository.save(sala));
    }

    public Sala buscarEntidadeAtiva(UUID id) {
        return salaRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Sala não encontrada ou foi inativada."));
    }

    public void validarSalaAtiva(UUID id) {
        Sala sala = buscarEntidadeAtiva(id);
        if (sala.getStatus() != StatusSala.ATIVA) {
            throw new RegraNegocioException(
                    "A sala não está ativa para receber reservas no momento.");
        }
    }

    private void validarNomeDuplicado(String nome, UUID idExcluir) {
        boolean duplicado = idExcluir == null
                ? salaRepository.existsByNomeIgnoreCaseAndDeletedAtIsNull(nome)
                : salaRepository.existsByNomeIgnoreCaseAndDeletedAtIsNullAndIdNot(nome, idExcluir);

        if (duplicado) {
            throw new RegraNegocioException(
                    "Já existe uma sala cadastrada com o nome informado.");
        }
    }
}
