package com.accenture.officehub.officehub_api.dto;

import java.util.List;

public record RoomPositionResponseDto(
        String code,
        String type,
        List<String> availableEquipment,
        boolean available
) {}
