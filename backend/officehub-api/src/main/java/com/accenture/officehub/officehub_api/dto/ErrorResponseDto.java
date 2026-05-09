package com.accenture.officehub.officehub_api.dto;

import java.time.OffsetDateTime;

public record ErrorResponseDto(
        OffsetDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {}
