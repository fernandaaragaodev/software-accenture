package com.accenture.officehub.officehub_api.dto;

import jakarta.validation.constraints.NotBlank;

public record RoomBlockRequestDto(
        @NotBlank(message = "adminPassword e obrigatorio.")
        String adminPassword
) {}
