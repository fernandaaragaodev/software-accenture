package com.accenture.officehub_v1.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ValidacaoSenhaRequest(
        @NotBlank(message = "Informe sua senha para confirmar a ação.") String senha
) {
}
