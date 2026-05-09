package com.accenture.officehub.officehub_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReservationRequestDto(
        @NotNull(message = "roomId e obrigatorio.") Long roomId,
        @NotBlank String user,
        @NotBlank String date,
        @NotBlank String start,
        @NotBlank String end
) {}
