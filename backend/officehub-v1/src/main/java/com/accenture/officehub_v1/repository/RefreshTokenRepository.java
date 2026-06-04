package com.accenture.officehub_v1.repository;

import com.accenture.officehub_v1.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHashAndRevogadoEmIsNull(String tokenHash);

    void deleteByUsuario_Id(UUID usuarioId);
}
