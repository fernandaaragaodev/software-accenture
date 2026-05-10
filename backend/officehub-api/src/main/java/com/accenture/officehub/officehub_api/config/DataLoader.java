package com.accenture.officehub.officehub_api.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.accenture.officehub.officehub_api.model.Role;
import com.accenture.officehub.officehub_api.model.Usuario;
import com.accenture.officehub.officehub_api.repository.UsuarioRepository;

@Configuration
public class DataLoader {

	@Bean
	@SuppressWarnings("unused")
	CommandLineRunner seedDashboardUsers(UsuarioRepository repo, PasswordEncoder passwordEncoder) {
		return args -> {
			seedIfAbsent(repo, passwordEncoder,
					"Rafael Torres", "admin@officehub.local", "admin", Role.ADMIN);
			seedIfAbsent(repo, passwordEncoder,
					"Maria Souza", "gestor@officehub.local", "gestor", Role.GESTOR);
			seedIfAbsent(repo, passwordEncoder,
					"Carlos Lima", "func@officehub.local", "func", Role.FUNCIONARIO);
		};
	}

	private static void seedIfAbsent(
			UsuarioRepository repo,
			PasswordEncoder passwordEncoder,
			String nome,
			String email,
			String rawPassword,
			Role role) {
		if (repo.existsByEmail(email)) {
			return;
		}
		Usuario u = new Usuario();
		u.setNome(nome);
		u.setEmail(email);
		u.setSenha(passwordEncoder.encode(rawPassword));
		u.setRole(role);
		repo.save(u);
	}
}
