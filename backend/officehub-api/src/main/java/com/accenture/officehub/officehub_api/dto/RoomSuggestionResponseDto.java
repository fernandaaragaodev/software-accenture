package com.accenture.officehub.officehub_api.dto;

import java.util.List;

public record RoomSuggestionResponseDto(
        Long roomId,
        String name,
        String floor,
        double score,
        int freeDesksInInterval,
        List<String> scoreReasons
) {}
