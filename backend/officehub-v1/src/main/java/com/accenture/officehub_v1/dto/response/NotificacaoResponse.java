package com.accenture.officehub_v1.dto.response;

import com.accenture.officehub_v1.entity.Notificacao;
import com.accenture.officehub_v1.entity.enums.StatusNotificacao;

import java.time.OffsetDateTime;
import java.util.UUID;

public record NotificacaoResponse(
        UUID id,
        UUID usuarioId,
        UUID reservaId,
        String tipo,
        String assunto,
        String mensagem,
        StatusNotificacao status,
        Integer tentativas,
        OffsetDateTime enviadoEm,
        OffsetDateTime createdAt
) {

    public static NotificacaoResponse from(Notificacao notificacao) {
        return new NotificacaoResponse(
                notificacao.getId(),
                notificacao.getUsuario().getId(),
                notificacao.getReserva() != null ? notificacao.getReserva().getId() : null,
                notificacao.getTipo(),
                notificacao.getAssunto(),
                notificacao.getMensagem(),
                notificacao.getStatus(),
                notificacao.getTentativas(),
                notificacao.getEnviadoEm(),
                notificacao.getCreatedAt()
        );
    }
}
