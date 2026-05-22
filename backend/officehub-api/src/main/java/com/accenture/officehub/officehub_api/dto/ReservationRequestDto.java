package com.accenture.officehub.officehub_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ReservationRequestDto(
        @NotNull(message = "roomId e obrigatorio.") Long roomId,
        @NotBlank String requesterName,
        @NotBlank String user,
        @NotBlank String requesterRole,
        @NotBlank String seatCode,
        @NotBlank String seatType,
        List<String> requestedEquipment,
        @NotBlank String date,
        @NotBlank String start,
        @NotBlank String end
) {}
