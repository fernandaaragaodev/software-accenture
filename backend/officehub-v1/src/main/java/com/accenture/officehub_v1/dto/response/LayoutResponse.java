package com.accenture.officehub_v1.dto.response;

import com.accenture.officehub_v1.entity.Layout;

import java.time.OffsetDateTime;
import java.util.UUID;

public record LayoutResponse(
        UUID id,
        UUID salaId,
        String versao,
        Boolean ativo,
        UUID aprovadoPorId,
        OffsetDateTime aprovadoEm,
        OffsetDateTime createdAt
) {

    public static LayoutResponse from(Layout layout) {
        return new LayoutResponse(
                layout.getId(),
                layout.getSala().getId(),
                layout.getVersao(),
                layout.getAtivo(),
                layout.getAprovadoPor() != null ? layout.getAprovadoPor().getId() : null,
                layout.getAprovadoEm(),
                layout.getCreatedAt()
        );
    }
}
