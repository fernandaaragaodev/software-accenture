package com.accenture.officehub_v1.security;

import com.accenture.officehub_v1.config.SecurityProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JwtService {

    private static final String CLAIM_ROLES = "roles";

    private final SecurityProperties securityProperties;

    public String gerarAccessToken(String email, List<String> roles) {
        Instant agora = Instant.now();
        Instant expiracao = agora.plusSeconds(securityProperties.jwt().expirationMinutes() * 60L);

        return Jwts.builder()
                .subject(email)
                .claim(CLAIM_ROLES, roles)
                .issuedAt(Date.from(agora))
                .expiration(Date.from(expiracao))
                .signWith(chaveSecreta())
                .compact();
    }

    public boolean validarToken(String token) {
        try {
            extrairClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public String extrairEmail(String token) {
        return extrairClaims(token).getSubject();
    }

    @SuppressWarnings("unchecked")
    public List<String> extrairRoles(String token) {
        Claims claims = extrairClaims(token);
        Object roles = claims.get(CLAIM_ROLES);
        if (roles instanceof List<?> lista) {
            return lista.stream().map(String::valueOf).toList();
        }
        return List.of();
    }

    private Claims extrairClaims(String token) {
        return Jwts.parser()
                .verifyWith(chaveSecreta())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey chaveSecreta() {
        byte[] bytes = securityProperties.jwt().secret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(bytes);
    }
}
