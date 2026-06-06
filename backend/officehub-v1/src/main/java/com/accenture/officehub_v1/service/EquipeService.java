package com.accenture.officehub_v1.service;

import com.accenture.officehub_v1.dto.request.AdicionarMembroEquipeRequest;
import com.accenture.officehub_v1.dto.request.CriarEquipeRequest;
import com.accenture.officehub_v1.dto.response.EquipeResumoResponse;
import com.accenture.officehub_v1.dto.response.EquipeResponse;
import com.accenture.officehub_v1.entity.Equipe;
import com.accenture.officehub_v1.entity.EquipeGestor;
import com.accenture.officehub_v1.entity.EquipeGestorId;
import com.accenture.officehub_v1.entity.EquipeMembro;
import com.accenture.officehub_v1.entity.EquipeMembroId;
import com.accenture.officehub_v1.entity.Usuario;
import com.accenture.officehub_v1.exception.AcessoNegadoException;
import com.accenture.officehub_v1.exception.RecursoNaoEncontradoException;
import com.accenture.officehub_v1.exception.RegraNegocioException;
import com.accenture.officehub_v1.repository.EquipeGestorRepository;
import com.accenture.officehub_v1.repository.EquipeMembroRepository;
import com.accenture.officehub_v1.repository.EquipeRepository;
import com.accenture.officehub_v1.repository.UsuarioPerfilRepository;
import com.accenture.officehub_v1.repository.UsuarioRepository;
import com.accenture.officehub_v1.security.Roles;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EquipeService {

    private final EquipeRepository equipeRepository;
    private final EquipeGestorRepository equipeGestorRepository;
    private final EquipeMembroRepository equipeMembroRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioPerfilRepository usuarioPerfilRepository;
    private final AuditService auditService;

    @Transactional
    public EquipeResponse criar(CriarEquipeRequest request, UUID usuarioId, Collection<String> perfis) {
        validarPermissaoGestao(perfis);

        UUID gestorId = resolverGestorId(request.gestorId(), usuarioId, perfis);
        Usuario gestor = buscarUsuarioAtivo(gestorId);
        validarPerfilGestor(gestorId);

        Equipe equipe = equipeRepository.save(Equipe.builder()
                .nome(request.nome().trim())
                .descricao(request.descricao())
                .build());

        vincularGestor(equipe, gestor);

        for (UUID membroId : request.membrosIds()) {
            if (membroId.equals(gestorId)) {
                throw new RegraNegocioException("O gestor da equipe não pode ser adicionado como membro.");
            }
            adicionarMembroInterno(equipe, membroId);
        }

        auditService.registrar(usuarioId, "CRIAR", "Equipe", equipe.getId());

        return EquipeResponse.from(carregarEquipeCompleta(equipe.getId()));
    }

    @Transactional
    public EquipeResponse adicionarMembro(
            UUID equipeId,
            AdicionarMembroEquipeRequest request,
            UUID usuarioId,
            Collection<String> perfis) {

        Equipe equipe = buscarEquipeAtiva(equipeId);
        validarPermissaoNaEquipe(equipeId, usuarioId, perfis);
        adicionarMembroInterno(equipe, request.usuarioId());
        auditService.registrar(usuarioId, "ADICIONAR_MEMBRO", "Equipe", equipeId);

        return EquipeResponse.from(carregarEquipeCompleta(equipeId));
    }

    @Transactional
    public EquipeResponse removerMembro(
            UUID equipeId,
            UUID membroId,
            UUID usuarioId,
            Collection<String> perfis) {

        buscarEquipeAtiva(equipeId);
        validarPermissaoNaEquipe(equipeId, usuarioId, perfis);

        if (!equipeMembroRepository.existsByEquipeIdAndUsuarioId(equipeId, membroId)) {
            throw new RecursoNaoEncontradoException(
                    "Funcionário não encontrado nesta equipe.");
        }

        equipeMembroRepository.deleteByEquipeIdAndUsuarioId(equipeId, membroId);
        auditService.registrar(usuarioId, "REMOVER_MEMBRO", "Equipe", equipeId);

        return EquipeResponse.from(carregarEquipeCompleta(equipeId));
    }

    @Transactional
    public void desmembrar(UUID equipeId, UUID usuarioId, Collection<String> perfis) {
        Equipe equipe = buscarEquipeAtiva(equipeId);
        validarPermissaoNaEquipe(equipeId, usuarioId, perfis);

        equipe.setDeletedAt(OffsetDateTime.now());
        equipeRepository.save(equipe);
        auditService.registrar(usuarioId, "DESMEMBRAR", "Equipe", equipe.getId());
    }

    public List<EquipeResumoResponse> listar(UUID usuarioId, Collection<String> perfis) {
        validarPermissaoGestao(perfis);

        List<Equipe> equipes = isAdmin(perfis)
                ? equipeRepository.findByDeletedAtIsNullOrderByNomeAsc()
                : equipeRepository.findAtivasPorGestor(usuarioId);

        return equipes.stream()
                .map(this::inicializarRelacionamentos)
                .map(EquipeResumoResponse::from)
                .toList();
    }

    public List<EquipeResumoResponse> listarMinhasEquipes(UUID usuarioId) {
        return equipeRepository.findAtivasPorMembro(usuarioId).stream()
                .map(this::inicializarRelacionamentos)
                .map(EquipeResumoResponse::from)
                .toList();
    }

    public EquipeResponse buscarPorId(UUID equipeId, UUID usuarioId, Collection<String> perfis) {
        Equipe equipe = carregarEquipeCompleta(equipeId);

        if (isAdmin(perfis) || equipeGestorRepository.existsByEquipeIdAndUsuarioId(equipeId, usuarioId)) {
            return EquipeResponse.from(equipe);
        }

        if (perfis.contains(Roles.USUARIO_FINAL)
                && equipeMembroRepository.existsByEquipeIdAndUsuarioId(equipeId, usuarioId)) {
            return EquipeResponse.from(equipe);
        }

        if (perfis.contains(Roles.GESTOR_RESERVAS)) {
            throw new AcessoNegadoException("Você não tem permissão para visualizar esta equipe.");
        }

        throw new AcessoNegadoException("Você não tem permissão para visualizar esta equipe.");
    }

    private Equipe carregarEquipeCompleta(UUID equipeId) {
        return inicializarRelacionamentos(buscarEquipeAtiva(equipeId));
    }

    private Equipe inicializarRelacionamentos(Equipe equipe) {
        equipe.getGestores().forEach(g -> g.getUsuario().getNome());
        equipe.getMembros().forEach(m -> m.getUsuario().getNome());
        return equipe;
    }

    private Equipe buscarEquipeAtiva(UUID equipeId) {
        return equipeRepository.findByIdAndDeletedAtIsNull(equipeId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Equipe não encontrada ou foi desmembrada."));
    }

    private Usuario buscarUsuarioAtivo(UUID usuarioId) {
        return usuarioRepository.findByIdAndDeletedAtIsNull(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Usuário não encontrado."));
    }

    private void adicionarMembroInterno(Equipe equipe, UUID membroId) {
        Usuario membro = buscarUsuarioAtivo(membroId);

        if (equipeMembroRepository.existsByEquipeIdAndUsuarioId(equipe.getId(), membro.getId())) {
            throw new RegraNegocioException("Este funcionário já faz parte da equipe.");
        }

        if (equipeMembroRepository.existsEmOutraEquipeAtiva(membro.getId(), equipe.getId())) {
            throw new RegraNegocioException(
                    "O funcionário já está vinculado a outra equipe ativa.");
        }

        if (equipeGestorRepository.existsByEquipeIdAndUsuarioId(equipe.getId(), membro.getId())) {
            throw new RegraNegocioException("O gestor da equipe não pode ser adicionado como membro.");
        }

        EquipeMembro vinculo = EquipeMembro.builder()
                .id(new EquipeMembroId(equipe.getId(), membro.getId()))
                .equipe(equipe)
                .usuario(membro)
                .build();

        equipeMembroRepository.save(vinculo);
    }

    private void vincularGestor(Equipe equipe, Usuario gestor) {
        EquipeGestor vinculo = EquipeGestor.builder()
                .id(new EquipeGestorId(equipe.getId(), gestor.getId()))
                .equipe(equipe)
                .usuario(gestor)
                .build();

        equipeGestorRepository.save(vinculo);
    }

    private UUID resolverGestorId(UUID gestorIdInformado, UUID usuarioId, Collection<String> perfis) {
        if (isAdmin(perfis)) {
            if (gestorIdInformado == null) {
                throw new RegraNegocioException(
                        "Informe o gestor responsável pela equipe.");
            }
            return gestorIdInformado;
        }

        if (gestorIdInformado != null && !gestorIdInformado.equals(usuarioId)) {
            throw new AcessoNegadoException(
                    "Gestor de reservas só pode criar equipe para si mesmo.");
        }

        return usuarioId;
    }

    private void validarPerfilGestor(UUID gestorId) {
        boolean possuiPerfil = usuarioPerfilRepository.findByUsuarioId(gestorId).stream()
                .anyMatch(up -> Roles.GESTOR_RESERVAS.equalsIgnoreCase(up.getPerfil().getNome()));

        if (!possuiPerfil) {
            throw new RegraNegocioException(
                    "O gestor informado deve possuir o perfil GESTOR_RESERVAS.");
        }
    }

    private void validarPermissaoGestao(Collection<String> perfis) {
        if (!isAdmin(perfis) && !perfis.contains(Roles.GESTOR_RESERVAS)) {
            throw new AcessoNegadoException(
                    "Apenas gestores de reservas ou administradores podem gerenciar equipes.");
        }
    }

    private void validarPermissaoNaEquipe(UUID equipeId, UUID usuarioId, Collection<String> perfis) {
        validarPermissaoGestao(perfis);

        if (!isAdmin(perfis) && !equipeGestorRepository.existsByEquipeIdAndUsuarioId(equipeId, usuarioId)) {
            throw new AcessoNegadoException(
                    "Você não tem permissão para gerenciar esta equipe.");
        }
    }

    private boolean isAdmin(Collection<String> perfis) {
        return perfis.contains(Roles.ADMIN_SALA);
    }
}
