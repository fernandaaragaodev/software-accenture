package com.accenture.officehub_v1.controller;

import com.accenture.officehub_v1.dto.request.CriarUsuarioRequest;
import com.accenture.officehub_v1.dto.request.LoginRequest;
import com.accenture.officehub_v1.dto.request.RefreshTokenRequest;
import com.accenture.officehub_v1.dto.response.LoginResponse;
import com.accenture.officehub_v1.dto.response.UsuarioResponse;
import com.accenture.officehub_v1.security.SecurityUtils;
import com.accenture.officehub_v1.service.AuthService;
import com.accenture.officehub_v1.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UsuarioService usuarioService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request.refreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        UUID usuarioId = obterUsuarioIdSeAutenticado();
        authService.logout(request.refreshToken(), usuarioId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/register")
    public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody CriarUsuarioRequest request) {
        UsuarioResponse response = authService.registrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> obterUsuarioAtual() {
        return ResponseEntity.ok(usuarioService.buscarPorId(SecurityUtils.getUsuarioIdAtual()));
    }

    private UUID obterUsuarioIdSeAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        try {
            return SecurityUtils.getUsuarioIdAtual();
        } catch (IllegalStateException ex) {
            return null;
        }
    }
}
