package com.accenture.officehub.officehub_api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.accenture.officehub.officehub_api.dto.LoginRequest;
import com.accenture.officehub.officehub_api.dto.LoginResponse;
import com.accenture.officehub.officehub_api.model.Usuario;
import com.accenture.officehub.officehub_api.repository.UsuarioRepository;
import com.accenture.officehub.officehub_api.security.JwtUtil;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

	private final UsuarioRepository usuarioRepository;
	private final JwtUtil jwtUtil;
	private final PasswordEncoder passwordEncoder;

	public AuthController(
			UsuarioRepository usuarioRepository,
			JwtUtil jwtUtil,
			PasswordEncoder passwordEncoder) {
		this.usuarioRepository = usuarioRepository;
		this.jwtUtil = jwtUtil;
		this.passwordEncoder = passwordEncoder;
	}

	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
		Usuario usuario = usuarioRepository.findByEmail(request.email())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas"));
		if (!passwordEncoder.matches(request.password(), usuario.getSenha())) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
		}
		String token = jwtUtil.gerarToken(usuario.getEmail());
		return ResponseEntity.ok(new LoginResponse(
				token,
				usuario.getNome(),
				usuario.getRole().toApiRole(),
				initials(usuario.getNome())));
	}

	private static String initials(String name) {
		if (name == null || name.isBlank()) {
			return "?";
		}
		String trimmed = name.trim();
		String[] parts = trimmed.split("\\s+");
		if (parts.length >= 2) {
			return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
		}
		return trimmed.length() >= 2 ? trimmed.substring(0, 2).toUpperCase() : trimmed.toUpperCase();
	}
}
