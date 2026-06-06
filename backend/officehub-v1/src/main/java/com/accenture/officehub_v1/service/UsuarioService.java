package com.accenture.officehub_v1.service;

import com.accenture.officehub_v1.dto.response.UsuarioResponse;
import com.accenture.officehub_v1.dto.response.UsuarioResumoResponse;
import com.accenture.officehub_v1.entity.Usuario;
import com.accenture.officehub_v1.exception.AcessoNegadoException;
import com.accenture.officehub_v1.exception.RecursoNaoEncontradoException;
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

    public List<UsuarioResponse> listar() {
        return usuarioRepository.findByDeletedAtIsNullAndAtivoTrueOrderByNomeAsc().stream()
                .map(this::mapearUsuario)
                .toList();
    }

    public List<UsuarioResumoResponse> listarGestores() {
        return usuarioPerfilRepository.findUsuariosAtivosPorPerfil(Roles.GESTOR_RESERVAS).stream()
                .map(UsuarioResumoResponse::from)
                .toList();
    }

    public List<UsuarioResumoResponse> listarDisponiveisParaEquipe() {
        return usuarioRepository.findByDeletedAtIsNullAndAtivoTrueOrderByNomeAsc().stream()
                .map(UsuarioResumoResponse::from)
                .toList();
    }

    public UsuarioResponse buscarPorId(UUID id) {
        Usuario usuario = usuarioRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));
        return mapearUsuario(usuario);
    }

    public List<UsuarioResumoResponse> listarMembrosEquipeDoGestor(UUID gestorId, Collection<String> perfis) {
        if (!perfis.contains(Roles.GESTOR_RESERVAS)) {
            throw new AcessoNegadoException(
                    "Apenas gestores de reservas podem listar membros da equipe.");
        }

        return equipeMembroRepository.findMembrosPorGestor(gestorId).stream()
                .map(UsuarioResumoResponse::from)
                .toList();
    }

    private UsuarioResponse mapearUsuario(Usuario usuario) {
        List<String> perfis = usuarioPerfilRepository.findByUsuarioId(usuario.getId()).stream()
                .map(up -> up.getPerfil().getNome())
                .toList();
        return UsuarioResponse.from(usuario, perfis);
    }
}
