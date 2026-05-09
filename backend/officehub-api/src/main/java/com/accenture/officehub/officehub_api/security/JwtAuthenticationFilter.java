package com.accenture.officehub.officehub_api.security;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.accenture.officehub.officehub_api.model.Usuario;
import com.accenture.officehub.officehub_api.repository.UsuarioRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtUtil jwtUtil;
	private final UsuarioRepository usuarioRepository;

	public JwtAuthenticationFilter(JwtUtil jwtUtil, UsuarioRepository usuarioRepository) {
		this.jwtUtil = jwtUtil;
		this.usuarioRepository = usuarioRepository;
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {

		String path = request.getRequestURI();
		if (path.startsWith("/h2-console") || path.equals("/auth/login")) {
			filterChain.doFilter(request, response);
			return;
		}

		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header == null || !header.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}

		String token = header.substring(7).trim();
		if (!jwtUtil.validateToken(token)) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido ou expirado");
			return;
		}

		String email = jwtUtil.getEmailFromToken(token);
		Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
		if (usuario == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Usuário não encontrado");
			return;
		}

		var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRole().name()));
		var authentication = new UsernamePasswordAuthenticationToken(
				usuario.getEmail(), null, authorities);
		authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
		SecurityContextHolder.getContext().setAuthentication(authentication);

		filterChain.doFilter(request, response);
	}
}
