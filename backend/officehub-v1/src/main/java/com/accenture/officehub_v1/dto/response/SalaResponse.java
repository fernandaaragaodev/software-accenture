package com.accenture.officehub_v1.dto.response;

import com.accenture.officehub_v1.entity.Sala;
import com.accenture.officehub_v1.entity.enums.StatusSala;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record SalaResponse(
        UUID id,
        String nome,
        String descricao,
        Integer andar,
        String bloco,
        Integer capacidadeMaxima,
        BigDecimal raioProximidade,
        StatusSala status,
        String imagemPath,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static SalaResponse from(Sala sala) {
        return new SalaResponse(
                sala.getId(),
                sala.getNome(),
                sala.getDescricao(),
                sala.getAndar(),
                sala.getBloco(),
                sala.getCapacidadeMaxima(),
                sala.getRaioProximidade(),
                sala.getStatus(),
                sala.getImagemPath(),
                sala.getCreatedAt(),
                sala.getUpdatedAt()
        );
    }
}
