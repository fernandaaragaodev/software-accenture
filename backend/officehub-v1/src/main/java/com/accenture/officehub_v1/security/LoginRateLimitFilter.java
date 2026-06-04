package com.accenture.officehub_v1.security;

import com.accenture.officehub_v1.config.SecurityProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private static final String LOGIN_PATH = "/api/v1/auth/login";

    private final SecurityProperties securityProperties;
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, List<Long>> tentativasPorIp = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        if (!"POST".equalsIgnoreCase(request.getMethod()) || !LOGIN_PATH.equals(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        String ip = obterIp(request);
        long agora = System.currentTimeMillis();
        long janelaMs = 60_000L;
        int limite = securityProperties.rateLimit().loginPerMinute();

        List<Long> tentativas = tentativasPorIp.computeIfAbsent(ip, chave -> new ArrayList<>());
        synchronized (tentativas) {
            tentativas.removeIf(instante -> agora - instante > janelaMs);
            if (tentativas.size() >= limite) {
                responderLimiteExcedido(response);
                return;
            }
            tentativas.add(agora);
        }

        filterChain.doFilter(request, response);
    }

    private void responderLimiteExcedido(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), Map.of(
                "timestamp", OffsetDateTime.now().toString(),
                "status", HttpStatus.TOO_MANY_REQUESTS.value(),
                "mensagem", "Limite de tentativas de login excedido. Tente novamente em instantes."));
    }

    private String obterIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
