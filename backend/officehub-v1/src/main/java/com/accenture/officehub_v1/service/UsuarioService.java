package com.accenture.officehub_v1.service;

import com.accenture.officehub_v1.dto.request.AtualizarUsuarioRequest;
import com.accenture.officehub_v1.dto.response.UsuarioResponse;
import com.accenture.officehub_v1.dto.response.UsuarioResumoResponse;
import com.accenture.officehub_v1.entity.Usuario;
import com.accenture.officehub_v1.exception.AcessoNegadoException;
import com.accenture.officehub_v1.exception.RecursoNaoEncontradoException;
import com.accenture.officehub_v1.exception.RegraNegocioException;
import com.accenture.officehub_v1.repository.EquipeMembroRepository;
import com.accenture.officehub_v1.repository.UsuarioPerfilRepository;
import com.accenture.officehub_v1.repository.UsuarioRepository;
import com.accenture.officehub_v1.security.Roles;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioPerfilRepository usuarioPerfilRepository;
    private final EquipeMembroRepository equipeMembroRepository;
    private final UsuarioVinculoService usuarioVinculoService;
    private final AuditService auditService;

    public List<UsuarioResponse> listar() {
        return usuarioRepository.findByDeletedAtIsNullAndAtivoTrueOrderByNomeAsc().stream()
                .map(this::inicializarRelacionamentos)
                .map(this::mapearUsuario)
                .toList();
    }

    public List<UsuarioResumoResponse> listarGestores() {
        return usuarioPerfilRepository.findUsuariosAtivosPorPerfil(Roles.GESTOR_RESERVAS).stream()
                .map(this::inicializarRelacionamentos)
                .map(UsuarioResumoResponse::from)
                .toList();
    }

    public List<UsuarioResumoResponse> listarDisponiveisParaEquipe() {
        return usuarioRepository.findByDeletedAtIsNullAndAtivoTrueOrderByNomeAsc().stream()
                .map(this::inicializarRelacionamentos)
                .map(UsuarioResumoResponse::from)
                .toList();
    }

    public UsuarioResponse buscarPorId(UUID id) {
        Usuario usuario = buscarUsuarioAtivo(id);
        return mapearUsuario(inicializarRelacionamentos(usuario));
    }

    public List<UsuarioResumoResponse> listarMembrosEquipeDoGestor(UUID gestorId, Collection<String> perfis) {
        if (!perfis.contains(Roles.GESTOR_RESERVAS)) {
            throw new AcessoNegadoException(
                    "Apenas gestores de reservas podem listar membros da equipe.");
        }

        return equipeMembroRepository.findMembrosPorGestor(gestorId).stream()
                .map(this::inicializarRelacionamentos)
                .map(UsuarioResumoResponse::from)
                .toList();
    }

    @Transactional
    public UsuarioResponse atualizar(UUID id, AtualizarUsuarioRequest request, UUID adminId) {
        Usuario usuario = buscarUsuarioAtivo(id);

        String emailNormalizado = request.email().trim().toLowerCase();
        if (!emailNormalizado.equalsIgnoreCase(usuario.getEmail())
                && usuarioRepository.existsByEmailIgnoreCaseAndIdNot(emailNormalizado, id)) {
            throw new RegraNegocioException("Já existe um usuário cadastrado com este e-mail.");
        }

        usuario.setEmail(emailNormalizado);
        usuarioVinculoService.atribuirCargo(usuario, request.cargoId());
        usuarioVinculoService.atribuirEspecialidades(usuario, request.especialidadeIds());

        usuario = usuarioRepository.save(usuario);
        auditService.registrar(adminId, "ATUALIZAR", "Usuario", usuario.getId());

        return mapearUsuario(inicializarRelacionamentos(usuario));
    }

    private Usuario buscarUsuarioAtivo(UUID id) {
        return usuarioRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));
    }

    private Usuario inicializarRelacionamentos(Usuario usuario) {
        UsuarioMapperHelper.inicializarCargoEEspecialidades(usuario);
        return usuario;
    }

    private UsuarioResponse mapearUsuario(Usuario usuario) {
        List<String> perfis = usuarioPerfilRepository.findByUsuarioId(usuario.getId()).stream()
                .map(up -> up.getPerfil().getNome())
                .toList();
        return UsuarioResponse.from(usuario, perfis);
    }
}
