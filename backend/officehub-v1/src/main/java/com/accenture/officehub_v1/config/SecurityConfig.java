package com.accenture.officehub_v1.config;

import com.accenture.officehub_v1.security.JsonAccessDeniedHandler;
import com.accenture.officehub_v1.security.JsonAuthenticationEntryPoint;
import com.accenture.officehub_v1.security.JwtAuthenticationFilter;
import com.accenture.officehub_v1.security.LoginRateLimitFilter;
import com.accenture.officehub_v1.security.Roles;
import com.accenture.officehub_v1.security.UsuarioDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(SecurityProperties.class)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final LoginRateLimitFilter loginRateLimitFilter;
    private final JsonAuthenticationEntryPoint authenticationEntryPoint;
    private final JsonAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> {})
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login", "/api/v1/auth/refresh")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/register")
                        .hasAuthority(Roles.ADMIN_SALA)
                        .requestMatchers("/api/v1/auth/**")
                        .authenticated()
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/api/docs/**",
                                "/api/v1/health",
                                "/api/v1/health/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/salas/*/disponibilidade")
                        .hasAnyAuthority(
                                Roles.USUARIO_FINAL,
                                Roles.INTEGRADOR,
                                Roles.GESTOR_RESERVAS,
                                Roles.ADMIN_SALA)
                        .requestMatchers(HttpMethod.GET, "/api/v1/salas/*/regras-disponibilidade")
                        .hasAnyAuthority(
                                Roles.USUARIO_FINAL,
                                Roles.INTEGRADOR,
                                Roles.GESTOR_RESERVAS,
                                Roles.ADMIN_SALA)
                        .requestMatchers(HttpMethod.GET, "/api/v1/salas", "/api/v1/salas/*")
                        .hasAnyAuthority(
                                Roles.USUARIO_FINAL,
                                Roles.INTEGRADOR,
                                Roles.GESTOR_RESERVAS,
                                Roles.ADMIN_SALA)
                        .requestMatchers(HttpMethod.POST, "/api/v1/reservas")
                        .hasAnyAuthority(
                                Roles.USUARIO_FINAL,
                                Roles.INTEGRADOR,
                                Roles.GESTOR_RESERVAS)
                        .requestMatchers(HttpMethod.GET, "/api/v1/reservas/*")
                        .hasAnyAuthority(Roles.USUARIO_FINAL, Roles.GESTOR_RESERVAS)
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/reservas/*")
                        .hasAnyAuthority(Roles.USUARIO_FINAL, Roles.GESTOR_RESERVAS)
                        .requestMatchers("/api/v1/reservas/**")
                        .hasAuthority(Roles.GESTOR_RESERVAS)
                        .requestMatchers("/api/v1/relatorios/**")
                        .hasAnyAuthority(Roles.GESTOR_RESERVAS, Roles.ADMIN_SALA)
                        .requestMatchers(HttpMethod.GET, "/api/v1/usuarios/membros-equipe")
                        .hasAuthority(Roles.GESTOR_RESERVAS)
                        .requestMatchers(HttpMethod.GET, "/api/v1/equipes/minhas")
                        .hasAuthority(Roles.USUARIO_FINAL)
                        .requestMatchers(HttpMethod.GET, "/api/v1/equipes", "/api/v1/equipes/*")
                        .hasAnyAuthority(Roles.GESTOR_RESERVAS, Roles.ADMIN_SALA, Roles.USUARIO_FINAL)
                        .requestMatchers("/api/v1/equipes/**")
                        .hasAnyAuthority(Roles.GESTOR_RESERVAS, Roles.ADMIN_SALA)
                        .requestMatchers("/api/v1/regras-disponibilidade/**")
                        .hasAuthority(Roles.ADMIN_SALA)
                        .requestMatchers("/api/v1/notificacoes/**")
                        .hasAuthority(Roles.GESTOR_RESERVAS)
                        .requestMatchers("/api/v1/usuarios/**")
                        .hasAnyAuthority(Roles.GESTOR_RESERVAS, Roles.ADMIN_SALA)
                        .requestMatchers(HttpMethod.GET, "/api/v1/tipos-equipamento")
                        .hasAnyAuthority(
                                Roles.USUARIO_FINAL,
                                Roles.INTEGRADOR,
                                Roles.GESTOR_RESERVAS,
                                Roles.ADMIN_SALA)
                        .requestMatchers(
                                "/api/v1/salas/**",
                                "/api/v1/posicoes/**",
                                "/api/v1/tipos-equipamento/**",
                                "/api/v1/layouts/**",
                                "/api/v1/ia/**")
                        .hasAuthority(Roles.ADMIN_SALA)
                        .anyRequest().authenticated())
                .addFilterBefore(loginRateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
