package com.accenture.officehub.officehub_api.dto;

public record NotificationResponseDto(
        Long id,
        String type,
        String text,
        String color,
        boolean read,
        String createdAt,
        Long reservationId,
        String reservationGroupId
) {}
