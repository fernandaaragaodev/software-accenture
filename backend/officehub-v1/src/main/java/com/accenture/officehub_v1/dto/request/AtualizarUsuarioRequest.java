package com.accenture.officehub_v1.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.UUID;

public record AtualizarUsuarioRequest(
        @NotBlank @Email String email,
        UUID cargoId,
        List<UUID> especialidadeIds
) {
}
