package com.accenture.officehub_v1.dto.response;

import java.util.List;
import java.util.UUID;

public record PosicaoDisponibilidadeItemResponse(
        UUID id,
        String identificador,
        String situacao,
        List<String> equipamentos
) {
}
