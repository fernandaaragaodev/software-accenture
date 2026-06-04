package com.accenture.officehub_v1.service;

import com.accenture.officehub_v1.config.SecurityProperties;
import com.accenture.officehub_v1.entity.RefreshToken;
import com.accenture.officehub_v1.entity.Usuario;
import com.accenture.officehub_v1.exception.RegraNegocioException;
import com.accenture.officehub_v1.repository.RefreshTokenRepository;
import com.accenture.officehub_v1.security.TokenHashUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final SecurityProperties securityProperties;

    @Transactional
    public String gerarRefreshToken(Usuario usuario) {
        refreshTokenRepository.deleteByUsuario_Id(usuario.getId());

        String token = UUID.randomUUID().toString() + UUID.randomUUID();
        String tokenHash = TokenHashUtil.hash(token);

        RefreshToken refreshToken = RefreshToken.builder()
                .usuario(usuario)
                .tokenHash(tokenHash)
                .expiraEm(OffsetDateTime.now().plusDays(securityProperties.refreshToken().expirationDays()))
                .build();

        refreshTokenRepository.save(refreshToken);
        return token;
    }

    @Transactional(readOnly = true)
    public Usuario validarRefreshToken(String token) {
        String tokenHash = TokenHashUtil.hash(token);

        RefreshToken refreshToken = refreshTokenRepository
                .findByTokenHashAndRevogadoEmIsNull(tokenHash)
                .orElseThrow(() -> new RegraNegocioException("Refresh token inválido ou revogado."));

        if (refreshToken.getExpiraEm().isBefore(OffsetDateTime.now())) {
            throw new RegraNegocioException("Refresh token expirado.");
        }

        return refreshToken.getUsuario();
    }

    @Transactional
    public void revogarRefreshToken(String token) {
        String tokenHash = TokenHashUtil.hash(token);
        refreshTokenRepository.findByTokenHashAndRevogadoEmIsNull(tokenHash)
                .ifPresent(refresh -> {
                    refresh.setRevogadoEm(OffsetDateTime.now());
                    refreshTokenRepository.save(refresh);
                });
    }
}
