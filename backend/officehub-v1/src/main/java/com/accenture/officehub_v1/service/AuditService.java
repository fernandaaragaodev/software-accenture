package com.accenture.officehub_v1.service;

import com.accenture.officehub_v1.entity.AuditLog;
import com.accenture.officehub_v1.entity.Usuario;
import com.accenture.officehub_v1.repository.AuditLogRepository;
import com.accenture.officehub_v1.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public void registrar(UUID usuarioId, String acao, String entidade, UUID entidadeId) {
        Usuario usuario = usuarioId != null
                ? usuarioRepository.findByIdAndDeletedAtIsNull(usuarioId).orElse(null)
                : null;

        AuditLog log = AuditLog.builder()
                .usuario(usuario)
                .acao(acao)
                .entidade(entidade)
                .entidadeId(entidadeId)
                .ipOrigem(obterIpOrigem())
                .userAgent(obterUserAgent())
                .build();

        auditLogRepository.save(log);
    }

    private String obterIpOrigem() {
        HttpServletRequest request = obterRequest();
        if (request == null) {
            return null;
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String obterUserAgent() {
        HttpServletRequest request = obterRequest();
        return request != null ? request.getHeader("User-Agent") : null;
    }

    private HttpServletRequest obterRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
}
