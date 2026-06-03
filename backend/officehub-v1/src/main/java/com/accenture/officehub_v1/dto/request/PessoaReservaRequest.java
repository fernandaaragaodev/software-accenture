package com.accenture.officehub_v1.dto.request;

import java.util.UUID;

public record PessoaReservaRequest(
        UUID usuarioId,
        String nomeExterno,
        String tipoPreferido1,
        String tipoPreferido2,
        String tipoPreferido3
) {
}
