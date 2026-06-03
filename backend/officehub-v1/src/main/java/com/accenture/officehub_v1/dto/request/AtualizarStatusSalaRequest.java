package com.accenture.officehub_v1.dto.request;

import com.accenture.officehub_v1.entity.enums.StatusSala;
import jakarta.validation.constraints.NotNull;

public record AtualizarStatusSalaRequest(
        @NotNull(message = "O status é obrigatório")
        StatusSala status
) {
}
