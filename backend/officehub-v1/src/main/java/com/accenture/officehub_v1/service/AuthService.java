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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
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

        Usuario usuario = Usuario.builder()
                .nome(request.nome())
                .email(request.email().trim().toLowerCase())
                .senhaHash(passwordEncoder.encode(request.senha()))
                .ativo(true)
                .build();

        usuario = usuarioRepository.save(usuario);

        List<String> perfisAtribuidos = atribuirPerfis(usuario, request.perfis());
        auditService.registrar(usuario.getId(), "REGISTER", "Usuario", usuario.getId());
        return UsuarioResponse.from(usuario, perfisAtribuidos);
    }

    private List<String> atribuirPerfis(Usuario usuario, List<String> perfisSolicitados) {
        Set<String> nomesPerfis = new LinkedHashSet<>();
        if (perfisSolicitados == null || perfisSolicitados.isEmpty()) {
            nomesPerfis.add(Roles.USUARIO_FINAL);
        } else {
            for (String perfilNome : perfisSolicitados) {
                if (!Roles.USUARIO_FINAL.equalsIgnoreCase(perfilNome)
                        && !Roles.GESTOR_RESERVAS.equalsIgnoreCase(perfilNome)) {
                    throw new RegraNegocioException(
                            "Perfis permitidos na criação: USUARIO_FINAL e GESTOR_RESERVAS.");
                }
                nomesPerfis.add(perfilNome.toUpperCase());
            }
        }

        List<String> perfisAtribuidos = new ArrayList<>();
        for (String nomePerfil : nomesPerfis) {
            Perfil perfil = perfilRepository.findByNomeIgnoreCase(nomePerfil)
                    .orElseThrow(() -> new RegraNegocioException(
                            "Perfil " + nomePerfil + " não encontrado no banco."));

            UsuarioPerfil usuarioPerfil = UsuarioPerfil.builder()
                    .id(new UsuarioPerfilId(usuario.getId(), perfil.getId()))
                    .usuario(usuario)
                    .perfil(perfil)
                    .build();
            usuarioPerfilRepository.save(usuarioPerfil);
            perfisAtribuidos.add(perfil.getNome());
        }

        return perfisAtribuidos;
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
