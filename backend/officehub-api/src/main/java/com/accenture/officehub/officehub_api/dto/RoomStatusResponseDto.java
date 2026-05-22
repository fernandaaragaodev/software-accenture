package com.accenture.officehub.officehub_api.dto;

import com.accenture.officehub.officehub_api.enums.RoomStatus;

public record RoomStatusResponseDto(
        Long roomId,
        RoomStatus status
) {}
