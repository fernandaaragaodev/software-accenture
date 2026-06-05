package com.accenture.officehub_v1.service;

import com.accenture.officehub_v1.dto.request.CriarUsuarioRequest;
import com.accenture.officehub_v1.dto.request.LoginRequest;
import com.accenture.officehub_v1.dto.response.LoginResponse;
import com.accenture.officehub_v1.dto.response.UsuarioResponse;
import com.accenture.officehub_v1.entity.Perfil;
import com.accenture.officehub_v1.entity.Usuario;
import com.accenture.officehub_v1.entity.UsuarioPerfil;
import com.accenture.officehub_v1.entity.UsuarioPerfilId;
import com.accenture.officehub_v1.exception.CredenciaisInvalidasException;
import com.accenture.officehub_v1.exception.RegraNegocioException;
import com.accenture.officehub_v1.repository.PerfilRepository;
import com.accenture.officehub_v1.repository.UsuarioPerfilRepository;
import com.accenture.officehub_v1.repository.UsuarioRepository;
import com.accenture.officehub_v1.security.JwtService;
import com.accenture.officehub_v1.security.Roles;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioPerfilRepository usuarioPerfilRepository;
    private final PerfilRepository perfilRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuditService auditService;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository
                .findByEmailIgnoreCaseAndDeletedAtIsNull(request.email())
                .orElseThrow(CredenciaisInvalidasException::new);

        if (!Boolean.TRUE.equals(usuario.getAtivo())
                || !passwordEncoder.matches(request.senha(), usuario.getSenhaHash())) {
            throw new CredenciaisInvalidasException();
        }

        LoginResponse response = emitirTokens(usuario);
        auditService.registrar(usuario.getId(), "LOGIN", "Usuario", usuario.getId());
        return response;
    }

    @Transactional
    public LoginResponse refresh(String refreshToken) {
        Usuario usuario = refreshTokenService.validarRefreshToken(refreshToken);
        refreshTokenService.revogarRefreshToken(refreshToken);
        return emitirTokens(usuario);
    }

    @Transactional
    public void logout(String refreshToken, UUID usuarioId) {
        refreshTokenService.revogarRefreshToken(refreshToken);
        if (usuarioId != null) {
            auditService.registrar(usuarioId, "LOGOUT", "Usuario", usuarioId);
        }
    }

    @Transactional
    public UsuarioResponse registrar(CriarUsuarioRequest request) {
        if (usuarioRepository.existsByEmailIgnoreCase(request.email())) {
            throw new RegraNegocioException("Já existe um usuário cadastrado com este e-mail.");
        }

        Usuario gestor = resolverGestor(request.gestorId());

        Usuario usuario = Usuario.builder()
                .nome(request.nome())
                .email(request.email().trim().toLowerCase())
                .senhaHash(passwordEncoder.encode(request.senha()))
                .ativo(true)
                .gestor(gestor)
                .build();

        usuario = usuarioRepository.save(usuario);

        Perfil perfil = perfilRepository.findByNomeIgnoreCase(Roles.USUARIO_FINAL)
                .orElseThrow(() -> new RegraNegocioException(
                        "Perfil padrão USUARIO_FINAL não encontrado no banco."));

        UsuarioPerfil usuarioPerfil = UsuarioPerfil.builder()
                .id(new UsuarioPerfilId(usuario.getId(), perfil.getId()))
                .usuario(usuario)
                .perfil(perfil)
                .build();
        usuarioPerfilRepository.save(usuarioPerfil);

        List<String> perfis = List.of(perfil.getNome());
        auditService.registrar(usuario.getId(), "REGISTER", "Usuario", usuario.getId());
        return UsuarioResponse.from(usuario, perfis);
    }

    private Usuario resolverGestor(UUID gestorId) {
        if (gestorId == null) {
            return null;
        }

        Usuario gestor = usuarioRepository.findByIdAndDeletedAtIsNull(gestorId)
                .orElseThrow(() -> new RegraNegocioException("Gestor informado não encontrado."));

        boolean possuiPerfilGestor = usuarioPerfilRepository.findByUsuarioId(gestorId).stream()
                .anyMatch(up -> Roles.GESTOR_RESERVAS.equalsIgnoreCase(up.getPerfil().getNome()));

        if (!possuiPerfilGestor) {
            throw new RegraNegocioException(
                    "O gestor informado deve possuir o perfil GESTOR_RESERVAS.");
        }

        return gestor;
    }

    private LoginResponse emitirTokens(Usuario usuario) {
        List<String> perfis = usuarioPerfilRepository.findByUsuarioId(usuario.getId()).stream()
                .map(up -> up.getPerfil().getNome())
                .toList();

        String accessToken = jwtService.gerarAccessToken(usuario.getEmail(), perfis);
        String refreshToken = refreshTokenService.gerarRefreshToken(usuario);
        return LoginResponse.of(accessToken, refreshToken);
    }
}
